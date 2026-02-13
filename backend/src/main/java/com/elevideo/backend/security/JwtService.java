package com.elevideo.backend.security;

import com.elevideo.backend.dto.JwtDataDto;
import com.elevideo.backend.enums.TokenPurpose;
import com.elevideo.backend.exception.TokenExpiredException;
import com.elevideo.backend.exception.TokenInvalidException;
import com.elevideo.backend.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTimeInMs;

    private SecretKey key;

    private final TokenBlacklistService blacklistService;

    @PostConstruct
    public void init() {
        if (secretKey.length() < 32) {
            throw new IllegalArgumentException("La clave secreta JWT debe tener al menos 32 caracteres.");
        }
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(JwtDataDto jwtData, TokenPurpose purpose) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationTimeInMs);

        return Jwts.builder()
                .subject(jwtData.id().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("email", jwtData.email())
                .claim("firstName",jwtData.firstName())
                .claim("lastName",jwtData.lastName())
                .claim("purpose", purpose.name())
                .signWith(key)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) &&
                !isTokenExpired(token) &&
                !blacklistService.isTokenBlacklisted(token);
    }

    public void validateEmailVerificationToken(String token) {

        if (isTokenExpired(token)) {
            throw new TokenExpiredException("El token de verificación de correo ha expirado. Solicita un nuevo enlace.");
        }

        if (blacklistService.isTokenBlacklisted(token)) {
            throw new TokenInvalidException("Este token ya fue utilizado y no puede volver a usarse.");
        }

        TokenPurpose purpose = extractPurpose(token);
        if (purpose != TokenPurpose.EMAIL_VERIFICATION) {
            throw new TokenInvalidException("El token no es válido para la verificación de correo electrónico.");
        }
    }

    public void validateResetPasswordToken(String token) {

        if (isTokenExpired(token)) {
            throw new TokenExpiredException("El token para restablecer la contraseña ha expirado. Solicita uno nuevo.");
        }

        if (blacklistService.isTokenBlacklisted(token)) {
            throw new TokenInvalidException("Este token ya fue utilizado y no puede reutilizarse.");
        }

        TokenPurpose purpose = extractPurpose(token);
        if (purpose != TokenPurpose.PASSWORD_RESET) {
            throw new TokenInvalidException("El token no es válido para la recuperación de contraseña.");
        }
    }



    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token,  Claims::getSubject));
    }

    public TokenPurpose extractPurpose(String token) {
        return TokenPurpose.valueOf(extractClaim(token, claims -> claims.get("purpose", String.class)));
    }

    public LocalDateTime extractExpiration(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return LocalDateTime.ofInstant(expiration.toInstant(), ZoneOffset.UTC);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).isBefore(LocalDateTime.now(ZoneOffset.UTC));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
