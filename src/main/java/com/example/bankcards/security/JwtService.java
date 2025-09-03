package com.example.bankcards.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Map;


@Service
public class JwtService {
    private final SecretKey key;
    private final long accessTtlMs;


    public JwtService(SecurityProps props) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secretBase64()));
        this.accessTtlMs = Duration.ofMinutes(props.accessMinutes()).toMillis();
    }


    public String generateAccessToken(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTtlMs))
                .signWith(key)
                .compact();
    }


    public Jws<Claims> parse(String jwt) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt);
    }
}