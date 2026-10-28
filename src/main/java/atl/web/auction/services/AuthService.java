package atl.web.auction.services;

import org.springframework.stereotype.Service;

import atl.web.auction.dto.auth.AuthRequest;
import atl.web.auction.dto.auth.AuthResponse;
import atl.web.auction.dto.auth.OAuth2Request;
import atl.web.auction.dto.auth.RegistrationRequest;
import atl.web.auction.dto.auth.RegistrationResponse;
import atl.web.auction.dto.auth.TokenRequest;
import atl.web.auction.dto.user.UserCreateRequest;
import atl.web.auction.model.User;

@Service
public interface AuthService {
    RegistrationResponse register(RegistrationRequest request);
    AuthResponse login(AuthRequest request);
    Boolean validateToken(TokenRequest request);
    Boolean isActiveToken(TokenRequest request);
    AuthResponse oauth2Login(OAuth2Request request);
    AuthResponse refreshToken(TokenRequest request);
    User createUser(UserCreateRequest request);
    void logout(String email);
} 
