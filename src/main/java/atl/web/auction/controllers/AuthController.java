package atl.web.auction.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import atl.web.auction.dto.auth.AuthRequest;
import atl.web.auction.dto.auth.AuthResponse;
import atl.web.auction.dto.auth.RegistrationRequest;
import atl.web.auction.dto.auth.RegistrationResponse;
import atl.web.auction.dto.auth.TokenRequest;
import atl.web.auction.jwt.UserDetailsImpl;
import atl.web.auction.services.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@SecurityRequirement(name = "Authorization")
public class AuthController {
    
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
        @Valid @RequestBody RegistrationRequest request
    ){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRequest request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validate(@Valid @RequestBody TokenRequest request){
        return ResponseEntity.ok(authService.validateToken(request));
    }

    @PostMapping("/is-active")
    public ResponseEntity<Boolean> isActiveToken(@Valid @RequestBody TokenRequest request){
        return ResponseEntity.ok(authService.isActiveToken(request));
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('ADMIN','USER','SELLER')")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
        authService.logout(userDetailsImpl.getUsername());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}   
