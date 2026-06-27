package com.yowyob.auth.application.service;

import com.yowyob.auth.application.port.in.AuthUseCase;
import com.yowyob.auth.application.port.out.PasswordEncoderPort;
import com.yowyob.auth.application.port.out.TokenServicePort;
import com.yowyob.auth.application.port.out.UserRepositoryPort;
import com.yowyob.auth.domain.model.User;
import com.yowyob.auth.dto.LoginRequest;
import com.yowyob.auth.dto.LoginResponse;
import com.yowyob.auth.dto.RegisterRequest;
import com.yowyob.auth.exception.EmailAlreadyExistsException;
import com.yowyob.auth.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApplicationService implements AuthUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenServicePort tokenServicePort;

    @Override
    public User register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepositoryPort.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoderPort.encode(request.getPassword()))
                .role(User.Role.USER)
                .status(User.Status.ACTIVE)
                .emailVerified(false)
                .build();

        return userRepositoryPort.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoderPort.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenServicePort.generateToken(user);

        return LoginResponse.builder()
                .accessToken(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}
