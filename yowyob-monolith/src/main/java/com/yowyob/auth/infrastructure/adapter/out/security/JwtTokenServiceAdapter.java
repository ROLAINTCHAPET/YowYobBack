package com.yowyob.auth.infrastructure.adapter.out.security;

import com.yowyob.auth.application.port.out.TokenServicePort;
import com.yowyob.auth.domain.model.User;
import com.yowyob.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenServiceAdapter implements TokenServicePort {

    private final JwtService jwtService;

    @Override
    public String generateToken(User user) {
        return jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
    }

    @Override
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    @Override
    public String getEmailFromToken(String token) {
        return jwtService.extractUsername(token);
    }
}
