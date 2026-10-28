package atl.web.auction.services.Impl.auth;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import atl.web.auction.dto.auth.AuthRequest;
import atl.web.auction.dto.auth.AuthResponse;
import atl.web.auction.dto.auth.OAuth2Request;
import atl.web.auction.dto.auth.RegistrationRequest;
import atl.web.auction.dto.auth.RegistrationResponse;
import atl.web.auction.dto.auth.TokenRequest;
import atl.web.auction.dto.user.UserCreateRequest;
import atl.web.auction.exceptions.auth.CredentialNotFoundException;
import atl.web.auction.exceptions.auth.EmailAlreadyExists;
import atl.web.auction.exceptions.auth.IncorrectPasswordOrEmailException;
import atl.web.auction.exceptions.auth.RefreshTokenNotFoundException;
import atl.web.auction.jwt.JwtUtils;
import atl.web.auction.model.Credential;
import atl.web.auction.model.RefreshToken;
import atl.web.auction.model.User;
import atl.web.auction.model.util.Role;
import atl.web.auction.repositories.CredentialRepository;
import atl.web.auction.repositories.RefreshTokenRepository;
import atl.web.auction.repositories.UserRepository;
import atl.web.auction.services.AuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class DefaultAuthService implements AuthService {

    private final JwtUtils jwtUtils;

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder encoder;

    @PostConstruct
    public void checkAdmins(){
        Stream<Credential> credentials = credentialRepository.findAll().stream()
            .filter(x->x.getRole() == Role.ROLE_ADMIN); 

        if(credentials.count() != 0){
            return;
        }    

        Credential admin = new Credential(null, "admin@gmail.com", encoder.encode("password"), Role.ROLE_ADMIN, null, null);
        User user = new User(null, admin, "name", "lastname", null, null, null, null);
    
        userRepository.save(user);
    }

    @Override
    public Boolean isActiveToken(TokenRequest request) {
        Boolean isActive = jwtUtils.isActiveToken(request.getToken());
        log.debug(isActive ? "Token " + request.getToken() + " is active"
                : "Token " + request.getToken() + " is not active");
        return isActive;
    }

    @Override
    public Boolean validateToken(TokenRequest request) {
        Boolean isValid = jwtUtils.validateToken(request.getToken());
        log.debug(isValid ? "Token " + request.getToken() + " is valid"
                : "Token " + request.getToken() + " is not valid");
        return isValid;
    }

    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        Credential credential = credentialRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: email not found '{}'", request.getEmail());
                    return new CredentialNotFoundException(request.getEmail());
                });

        if (!encoder.matches(request.getPassword(), credential.getHashPassword())) {
            throw new IncorrectPasswordOrEmailException();
        }

        String accessToken = jwtUtils.generateAccessToken(credential);
        String refreshToken = jwtUtils.generateRefreshToken(credential);
        RefreshToken token = null;
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByCredential(credential);

        if (optionalToken.isPresent()) {
            token = optionalToken.get();
        }
        if (token == null) {
            log.debug("Creating new refresh token for user: {}", credential.getEmail());
            token = new RefreshToken(null, refreshToken, credential, true);
        }
        refreshTokenRepository.save(token);
        log.info("User successfully authenticated: {}", credential.getEmail());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        if (credentialRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email already exists '{}'", request.getEmail());
            throw new EmailAlreadyExists(request.getEmail());
        }
        Credential credential = new Credential(
                null,
                request.getEmail(),
                encoder.encode(request.getPassword()),
                Role.ROLE_USER,
                null,
                null);
        User user = new User(null,
                credential,
                request.getFirstname(),
                request.getLastname(),
                null,
                null,
                null,
                null);
        credential.setUser(user);
        User savedUser = userRepository.save(user);
        log.info("User with email '{}' registered", request.getEmail());
        return RegistrationResponse.builder()
                .email(savedUser.getCredential().getEmail())
                .role(savedUser.getCredential().getRole())
                .firstname(savedUser.getFirstname())
                .lastname(savedUser.getLastname())
                .id(savedUser.getId())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse oauth2Login(OAuth2Request request) {
        Optional<Credential> optionalCredential = credentialRepository
                .findByEmailWithJoins(request.getEmail());

        Credential credential = null;
        if (!optionalCredential.isPresent()) {
            credential = new Credential(
                    null,
                    request.getEmail(),
                    null,
                    Role.ROLE_USER,
                    null,
                    null);

            User user = new User(null,
                    credential,
                    request.getFirstname(),
                    request.getLastname(),
                    null,
                    null,
                    null,
                    null);

            credential.setUser(user);
            userRepository.save(user);

            log.debug("OAuth2 login: user registered in db with email '{}'", request.getEmail());
        } else {
            credential = optionalCredential.get();
        }

        String accessToken = jwtUtils.generateAccessToken(credential);
        String refreshToken = jwtUtils.generateRefreshToken(credential);

        RefreshToken token = null;
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByCredential(credential);

        if (optionalToken.isPresent()) {
            token = optionalToken.get();
        }

        if (token == null) {
            token = new RefreshToken(null, refreshToken, credential, true);
        }

        refreshTokenRepository.save(token);

        log.info("User authenticated through Google with email '{}'", request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(TokenRequest request) {
        RefreshToken token = refreshTokenRepository
                .findByRefreshToken(request.getToken())
                .orElseThrow(() -> {
                    log.warn("Refresh Token failed: token not found");
                    return new RefreshTokenNotFoundException();
                });

        if (!token.getIsActive()) {
            log.warn("Refresh Token failed: token isn't active");
            throw new RefreshTokenNotFoundException();
        }

        Credential credential = token.getCredential();

        String accessToken = jwtUtils.generateAccessToken(credential);
        String refreshToken = jwtUtils.generateRefreshToken(credential);

        token.setRefreshToken(refreshToken);
        refreshTokenRepository.save(token);

        log.info("User with email '{}' refreshed token to {}", credential.getEmail(), refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(String email) {
        Credential credential = credentialRepository
                .findByEmailWithJoins(email)
                .orElseThrow(() -> {
                    log.warn("Logout failed: email not found {}", email);
                    return new CredentialNotFoundException(email);
                });

        RefreshToken refreshToken = credential.getRefreshToken();

        if (refreshToken == null || !refreshToken.getIsActive()) {
            log.warn("Refresh Token failed: token isn't active or not found");
            throw new RefreshTokenNotFoundException();
        }

        refreshToken.setIsActive(false);
        refreshTokenRepository.save(refreshToken);

        log.info("User with email '{}' logouted", email);
    }

    @Override
    @Transactional
    public User createUser(UserCreateRequest request) {
        if (credentialRepository.existsByEmail(request.getEmail())) {
            log.warn("Creating user failed: email already exists '{}'", request.getEmail());
            throw new EmailAlreadyExists(request.getEmail());
        }
        
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .build();

        Credential credential = new Credential(
                null,
                request.getEmail(),
                encoder.encode(request.getPassword()),
                request.getRole(),
                user,
                null);

        user.setCredential(credential);
        User savedUser = userRepository.save(user);

        log.info("User was created with id '{}'", savedUser.getId());

        return savedUser;
    }

}
