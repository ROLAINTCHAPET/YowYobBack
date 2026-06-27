package com.yowyob.auth.application.port.out;

import com.yowyob.auth.domain.model.User;

public interface TokenServicePort {
    String generateToken(User user);
    boolean validateToken(String token);
    String getEmailFromToken(String token);
}
