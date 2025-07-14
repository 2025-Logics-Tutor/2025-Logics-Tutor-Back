package com.ask_logic.back.jwt;

import com.ask_logic.back.domain.User;
import com.ask_logic.back.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access.exp}")
    private long ACCESS_EXP;

    @Value("${jwt.refresh.exp}")
    private long REFRESH_EXP;

    private Key key;

    @PostConstruct
    public void init() {
        if (SECRET_KEY == null) {
            throw new IllegalStateException("jwt.secret is null! application.properties 혹은 profile 설정을 확인하세요.");
        }
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String createAccessToken(String email) {
        return createToken(email, ACCESS_EXP);
    }

    public String createRefreshToken(String email) {
        return createToken(email, REFRESH_EXP);
    }

    private String createToken(String email, long expireTime) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public User getUser(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("잘못된 인증 헤더 형식입니다.");
        }

        String jwt = token.substring(7).trim();

        String email;
        try {
            email = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.", e);
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 유저가 존재하지 않습니다."));
    }
}
