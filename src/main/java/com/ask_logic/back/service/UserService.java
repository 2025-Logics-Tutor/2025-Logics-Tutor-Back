package com.ask_logic.back.service;

import com.ask_logic.back.domain.RefreshToken;
import com.ask_logic.back.domain.User;
import com.ask_logic.back.dto.request.LoginRequest;
import com.ask_logic.back.dto.request.SignupRequest;
import com.ask_logic.back.dto.response.TokenResponse;
import com.ask_logic.back.jwt.JwtUtil;
import com.ask_logic.back.repository.RefreshTokenRepository;
import com.ask_logic.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .level(request.getLevel())
                .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(user.getEmail());
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        refreshTokenRepository.save(RefreshToken.builder()
                .email(user.getEmail())
                .token(refreshToken)
                .build());

        return new TokenResponse(accessToken, refreshToken);
    }

    public void logout(String accessToken) {
        String email = jwtUtil.getEmail(accessToken.replace("Bearer ", ""));
        refreshTokenRepository.deleteById(email);
    }

    public TokenResponse reissue(String refreshToken) {
        if (!jwtUtil.isValid(refreshToken.replace("Bearer ", ""))) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = jwtUtil.getEmail(refreshToken.replace("Bearer ", ""));
        RefreshToken stored = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("저장된 리프레시 토큰이 없습니다."));

        if (!stored.getToken().equals(refreshToken.replace("Bearer ", ""))) {
            throw new IllegalArgumentException("토큰 불일치");
        }

        String newAccessToken = jwtUtil.createAccessToken(email);
        String newRefreshToken = jwtUtil.createRefreshToken(email);
        stored.updateToken(newRefreshToken);
        refreshTokenRepository.save(stored);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
