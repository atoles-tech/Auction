package atl.web.auction.services.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import atl.web.auction.dto.auth.AuthRequest;
import atl.web.auction.dto.auth.AuthResponse;
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
import atl.web.auction.services.Impl.auth.DefaultAuthService;

@ExtendWith(MockitoExtension.class)
public class DefaultAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private DefaultAuthService authService;

    private Credential credential;
    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    public void init() {
        credential = new Credential(1L, "email@gmail.com", null, Role.ROLE_USER, null, null);
        refreshToken = new RefreshToken(1L, null, credential, true);
        user = new User(1L, credential, "name", "lastname", null, null, null, null);
        credential.setUser(user);
        credential.setRefreshToken(refreshToken);
    }

    @Test
    public void givenAuthRequest_whenUserExistsAndRightPassword_thenReturnTokens() {
        // Given
        AuthRequest request = new AuthRequest("email@gmail.com", "password");

        // When
        when(credentialRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(credential));
        when(encoder.matches(request.getPassword(), credential.getHashPassword())).thenReturn(true);
        when(refreshTokenRepository.findByCredential(credential)).thenReturn(Optional.of(refreshToken));
        when(jwtUtils.generateAccessToken(credential)).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(credential)).thenReturn("refresh-token");

        // Then
        AuthResponse result = authService.login(request);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }

    @Test
    public void givenAuthRequest_whenUserIsNotExists_thenReturnTokens() {
        // Given
        AuthRequest request = new AuthRequest("email@gmail.com", "password");

        // When | Then
        when(credentialRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        assertThrows(CredentialNotFoundException.class, () -> authService.login(request));
    }

    @Test
    public void givenAuthRequest_whenUserExistsAndPasswordIsWrong_thenReturnTokens() {
        // Given
        AuthRequest request = new AuthRequest("email@gmail.com", "password");

        // When | Then
        when(credentialRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(credential));
        when(encoder.matches(request.getPassword(), credential.getHashPassword())).thenReturn(false);
        assertThrows(IncorrectPasswordOrEmailException.class, () -> authService.login(request));
    }

    @Test
    public void givenToken_whenRefreshTokenIsActive_thenReturnTokens() {
        // Given
        TokenRequest request = new TokenRequest("refresh-token");

        // When
        when(refreshTokenRepository.findByRefreshToken(request.getToken())).thenReturn(Optional.of(refreshToken));
        when(jwtUtils.generateAccessToken(credential)).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken(credential)).thenReturn("new-refresh-token");

        // Then
        AuthResponse response = authService.refreshToken(request);
        assertNotEquals(request.getToken(), response.getRefreshToken());
    }

    @Test
    public void givenToken_whenRefreshTokenIsNotActive_thenReturnTokens() {
        // Given
        TokenRequest request = new TokenRequest("refresh-token");
        refreshToken.setIsActive(false);

        // When | Then
        when(refreshTokenRepository.findByRefreshToken(request.getToken())).thenReturn(Optional.of(refreshToken));
        assertThrows(RefreshTokenNotFoundException.class, () -> authService.refreshToken(request));
    }

    @Test
    public void givenToken_whenRefreshTokenIsNotExists_thenReturnTokens() {
        // Given
        TokenRequest request = new TokenRequest("refresh-token");

        // When | Then
        when(refreshTokenRepository.findByRefreshToken(request.getToken())).thenReturn(Optional.empty());
        assertThrows(RefreshTokenNotFoundException.class, () -> authService.refreshToken(request));
    }

    @Test
    public void givenRegistrationRequest_whenEmailIsNotExists_thenRetunResponse() {
        // Given
        RegistrationRequest request = new RegistrationRequest("email@gmail.com", "password", "name", "lastname");

        // When
        when(credentialRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        // Then
        RegistrationResponse result = authService.register(request);
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getFirstname(), result.getFirstname());
    }

    @Test
    public void givenRegistrationRequest_whenEmailIsExists_thenRetunResponse() {
        // Given
        RegistrationRequest request = new RegistrationRequest("email@gmail.com", "password", "name", "lastname");

        // When | Then
        when(credentialRepository.existsByEmail(request.getEmail())).thenReturn(true);
        assertThrows(EmailAlreadyExists.class, () -> authService.register(request));
    }

    @Test
    public void givenEmail_whenUserExistsAndAuthenticated_thenSetRefreshTokenIsUnactive() {
        // Given
        String email = "email@gmail.com";

        // When
        when(credentialRepository.findByEmailWithJoins(email)).thenReturn(Optional.of(credential));
        when(refreshTokenRepository.save(refreshToken)).thenReturn(refreshToken);

        // Then
        authService.logout(email);
        assertFalse(refreshToken.getIsActive());
    }

    @Test
    public void givenEmail_whenUserExistsAndNotAuthenticated_thenSetRefreshTokenIsUnactive() {
        // Given
        String email = "email@gmail.com";
        refreshToken.setIsActive(false);

        // When | Then
        when(credentialRepository.findByEmailWithJoins(email)).thenReturn(Optional.of(credential));
        assertThrows(RefreshTokenNotFoundException.class, ()->authService.logout(email));
    }

    @Test
    public void givenEmail_whenUserIsNotExists_thenSetRefreshTokenIsUnactive() {
        // Given
        String email = "email@gmail.com";

        // When | Then
        when(credentialRepository.findByEmailWithJoins(email)).thenReturn(Optional.empty());
        assertThrows(CredentialNotFoundException.class, ()->authService.logout(email));
    }

    @Test
    public void givenUserCreateRequest_whenEmailIsNotExists_thenReturnUser(){
        // Given
        UserCreateRequest request = new UserCreateRequest("email@gmail.com","password","name","lastname",Role.ROLE_USER);
        
        // When
        when(credentialRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        // Then
        User result = authService.createUser(request);
        assertEquals(user, result);
    }

    @Test
    public void givenUserCreateRequest_whenEmailIsExists_thenReturnUser(){
        // Given
        UserCreateRequest request = new UserCreateRequest("email@gmail.com","password","name","lastname",Role.ROLE_USER);
        
        // When | Then
        when(credentialRepository.existsByEmail(request.getEmail())).thenReturn(true);
        assertThrows(EmailAlreadyExists.class, ()->authService.createUser(request));
    }

    @Test
    public void givenUserCreateRequestWithSellerRole_whenEmailIsNotExists_thenCreateSellerUser(){
        // Given
        UserCreateRequest request = new UserCreateRequest(
            "seller@gmail.com",
            "password",
            "seller",
            "lastname",
            Role.ROLE_SELLER
        );

        // When
        when(credentialRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(encoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        User result = authService.createUser(request);
        assertNotNull(result.getCredential());
        assertEquals(Role.ROLE_SELLER, result.getCredential().getRole());
    }
}
