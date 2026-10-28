package atl.web.auction.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import atl.web.auction.model.Credential;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-exp}")
    private Long accessTokenExp;

    @Value("${jwt.refresh-exp}")
    private Long refreshTokenExp;

    public String generateAccessToken(Credential credential){
        return Jwts.builder()
            .subject(credential.getEmail())
            .claim("role", credential.getRole())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenExp))
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public String generateRefreshToken(Credential credential){
        return Jwts.builder()
            .subject(credential.getEmail())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshTokenExp))
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public String getRoleFromToken(String token){
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("role",String.class);
    }

    public String getEmailFromToken(String token){
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public Boolean isActiveToken(String token){
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getExpiration()
            .after(new Date());
    }

    public Boolean validateToken(String token){
        try{
            Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return true;
        }catch(JwtException | IllegalArgumentException e){
            return false;
        }
    }
}
