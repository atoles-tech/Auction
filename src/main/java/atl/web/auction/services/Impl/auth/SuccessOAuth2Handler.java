package atl.web.auction.services.Impl.auth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import atl.web.auction.dto.auth.AuthResponse;
import atl.web.auction.dto.auth.OAuth2Request;
import atl.web.auction.services.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@Qualifier("successOAuth2Handler")
@RequiredArgsConstructor
public class SuccessOAuth2Handler implements AuthenticationSuccessHandler{

    private final AuthService authService;

    @Value("${frontend.url:http://localhost}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request, 
        HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
    
        DefaultOAuth2User user = (DefaultOAuth2User)authentication.getPrincipal();
        
        String email = user.getAttribute("email");
        String firstname = user.getAttribute("given_name");
        String lastname = user.getAttribute("family_name");
    
        AuthResponse authResponse = authService.oauth2Login(new OAuth2Request(email, firstname, lastname));

        String redirectUrl = String.format("%s/auth/callback?token=%s&refreshtoken=%s", 
            frontendUrl, 
            authResponse.getAccessToken(),
            authResponse.getRefreshToken());
        
        response.sendRedirect(redirectUrl);
    }
}
