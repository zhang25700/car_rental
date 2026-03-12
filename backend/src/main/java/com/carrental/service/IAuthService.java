package com.carrental.service;

import com.carrental.dto.LoginRequest;
import com.carrental.dto.RegisterRequest;
import com.carrental.entity.User;
import java.util.Map;

public interface IAuthService {
    Map<String, Object> login(LoginRequest request);

    void register(RegisterRequest request);

    Map<String, Object> sanitizeUser(User user);
}
