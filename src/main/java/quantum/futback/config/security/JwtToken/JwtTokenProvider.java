package quantum.futback.config.security.JwtToken;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quantum.futback.entity.RefreshToken;
import quantum.futback.entity.User;
import quantum.futback.repository.RefreshTokenRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expirationMs}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refreshExpirationMs}")
    private long jwtRefreshExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // JWT usa el ID del usuario como Subject, incluye Tenant ID y Role Name
        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .claim("tenantId", userPrincipal.getTenantId().toString())
                .claim("role", userPrincipal.getRoleName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    @Transactional
    public String generateAndSaveRefreshToken(User user) {
        // Genera un token aleatorio, no JWT, para simplicidad y seguridad
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(jwtRefreshExpirationMs);

        // Si ya existe un token para este usuario, lo eliminamos (permite un solo token activo)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken(user, token, expiryDate);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // Logear: Firma JWT inválida
        } catch (MalformedJwtException ex) {
            // Logear: Token JWT mal formado
        } catch (ExpiredJwtException ex) {
            // Logear: Token JWT expirado
        } catch (UnsupportedJwtException ex) {
            // Logear: Token JWT no soportado
        } catch (IllegalArgumentException ex) {
            // Logear: Cadena de claims JWT vacía
        }
        return false;
    }

    /**
     * Obtiene el ID de usuario del Access Token.
     */
    public UUID getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    /**
     * Obtiene el Refresh Token de la BD y verifica su expiración.
     */
    public Optional<RefreshToken> findByRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);

        if (refreshTokenOpt.isPresent() && refreshTokenOpt.get().getExpiryDate().isBefore(Instant.now())) {
            // Token expirado, lo eliminamos de la BD para limpiar
            refreshTokenRepository.delete(refreshTokenOpt.get());
            return Optional.empty();
        }
        return refreshTokenOpt;
    }

    // Inyección del Repositorio
    public final RefreshTokenRepository refreshTokenRepository;

    public JwtTokenProvider(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }
}
