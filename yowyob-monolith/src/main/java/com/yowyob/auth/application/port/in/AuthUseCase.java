package com.yowyob.auth.application.port.in;

import com.yowyob.auth.domain.model.User;
import com.yowyob.auth.dto.LoginRequest;
import com.yowyob.auth.dto.LoginResponse;
import com.yowyob.auth.dto.RegisterRequest;

public interface AuthUseCase {
    User register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
