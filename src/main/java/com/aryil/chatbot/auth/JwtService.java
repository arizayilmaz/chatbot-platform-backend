package com.aryil.chatbot.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getExpiresMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(user.getId().toString())          // userId = subject
                .claim("role", user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secretBytes()))
                .compact();
    }

    public UUID parseUserId(String token) {
        String sub = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return UUID.fromString(sub);
    }

    private byte[] secretBytes() {
        // Base64 değil, düz string secret kullanıyorsan bu yeterli.
        // Secret'ın uzun olması önemli.
        return props.getSecret().getBytes(StandardCharsets.UTF_8);
    }
    public String parseRole(String token) {
        Object role = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role");
        return role == null ? "USER" : role.toString();
    }
}
