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

    @Value("${JWT_SECRET}")
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
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(jwtRefreshExpirationMs);

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

    public UUID getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    public Optional<RefreshToken> findByRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);

        if (refreshTokenOpt.isPresent() && refreshTokenOpt.get().getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshTokenOpt.get());
            return Optional.empty();
        }
        return refreshTokenOpt;
    }

    @Transactional
    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtTokenProvider(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public UUID getTenantIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String tenantIdString = claims.get("tenantId", String.class);

        if (tenantIdString == null) {
            return null;
        }

        return UUID.fromString(tenantIdString);
    }
}