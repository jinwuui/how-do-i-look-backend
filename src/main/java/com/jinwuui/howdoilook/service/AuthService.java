package com.jinwuui.howdoilook.service;

import com.jinwuui.howdoilook.domain.User;
import com.jinwuui.howdoilook.dto.service.SignUpDto;
import com.jinwuui.howdoilook.dto.service.TokenDto;
import com.jinwuui.howdoilook.exception.AlreadyExistsEmailException;
import com.jinwuui.howdoilook.exception.InvalidTokenException;
import com.jinwuui.howdoilook.repository.UserRepository;
import com.jinwuui.howdoilook.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private JwtUtil jwtUtil;

    public void signUp(SignUpDto signUpDto) {
        Optional<User> userOptional = userRepository.findByEmail(signUpDto.getEmail());
        if (userOptional.isPresent()) {
            throw new AlreadyExistsEmailException();
        }

        User user = User.builder()
                .email(signUpDto.getEmail())
                .password(signUpDto.getPassword())
                .nickname(signUpDto.getNickname())
                .build();
        userRepository.save(user);
    }

    public TokenDto generateTokens(String refreshToken) {
        if (refreshToken == null)
            throw new InvalidTokenException();

        String email = jwtUtil.extractEmail(refreshToken);
        if (jwtUtil.isTokenNotValid(refreshToken, email))
            throw new InvalidTokenException();

        String newAccessToken = jwtUtil.generateAccessToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);
        return TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
