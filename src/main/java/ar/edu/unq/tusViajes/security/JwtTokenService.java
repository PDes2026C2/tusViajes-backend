package ar.edu.unq.tusViajes.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ar.edu.unq.tusViajes.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {

    private final SecretKey key;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtTokenService(
            @Value("${jwt.secret:tusViajesSuperSecretKeyForJwtSigningMustBeAtLeast256BitsLong2026!}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs,
            @Value("${jwt.refresh-expiration-ms:86400000}") long refreshExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;

    }

    public String generarToken(Usuario usuario, Boolean isRefresh) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + (isRefresh ? refreshExpirationMs : expirationMs));

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getIdentificadorVisual())
                .claim("type", isRefresh ? "refresh" : "access")
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(key)
                .compact();
    }

    public String generarToken(Usuario usuario) {
        return generarToken(usuario, false);
    }

    public String obtenerEmail(String token) {
        return obtenerClaims(token).getSubject();
    }

    public boolean esValido(String token) {
        try {
            Claims claims = obtenerClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean esRefreshToken(String token) {
        try {
            Claims claims = obtenerClaims(token);
            return "refresh".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
