package com.medsync.security;

import com.medsync.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component   // Spring manages this as a singleton bean — one instance, shared everywhere
@Slf4j       // Lombok injects: private static final Logger log = LoggerFactory.getLogger(...)
public class JwtTokenProvider {

    // @Value reads from application.yml: jwt.secret and jwt.expiration
    // Spring injects these at startup — we never hardcode secrets in code
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    // ── Token Generation ─────────────────────────────────────────────
    // Called once when a user successfully logs in.
    // Returns a signed JWT string that the frontend stores and sends
    // in the Authorization header on every subsequent request.
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            // "subject" = who this token belongs to (we use email as the identifier)
            .setSubject(user.getEmail())
            // Custom claims — extra data we embed in the token payload.
            // The frontend can decode these (they're Base64, not encrypted)
            // to know the user's role without making an extra API call.
            .claim("role", user.getRole().name())
            .claim("userId", user.getId())
            .claim("fullName", user.getFullName())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            // Signs the token with our secret key using HMAC-SHA256.
            // Without a valid signature, the token is rejected.
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();   // serialises everything into a single "xxxxx.yyyyy.zzzzz" string
    }

    // ── Token Validation ─────────────────────────────────────────────
    // Called on every incoming request by JwtAuthenticationFilter.
    // Returns true only if the token is properly signed AND not expired.
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);   // throws if invalid or expired
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // ── Extract Email from Token ──────────────────────────────────────
    // After validating, we extract the email (subject) to load the User
    // from the database in CustomUserDetailsService.
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()       // the payload section of the JWT
            .getSubject();   // returns the email we set in generateToken()
    }

    // ── Signing Key ───────────────────────────────────────────────────
    // Converts our hex secret string into a cryptographic Key object.
    // Keys.hmacShaKeyFor() ensures the key meets minimum length requirements.
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}