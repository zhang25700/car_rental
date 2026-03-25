package com.carrental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.BusinessException;
import com.carrental.dto.LoginRequest;
import com.carrental.dto.RegisterRequest;
import com.carrental.entity.User;
import com.carrental.mapper.UserMapper;
import com.carrental.service.IAuthService;
import com.carrental.util.JwtUtils;
import com.carrental.util.PasswordUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    @Override
    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null || !user.getPassword().equals(PasswordUtils.encode(request.getPassword()))) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被冻结");
        }
        String token = jwtUtils.createToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", sanitizeUser(user));
        return result;
    }

    @Override
    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole("CUSTOMER");
        user.setStatus(1);
        user.setVerifyStatus("PENDING");
        user.setVerifyRemark("请尽快补充身份证信息，等待人工审核");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public Map<String, Object> sanitizeUser(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("role", user.getRole());
        result.put("status", user.getStatus());
        result.put("idCardNo", user.getIdCardNo());
        result.put("idCardFront", user.getIdCardFront());
        result.put("idCardBack", user.getIdCardBack());
        result.put("verifyStatus", user.getVerifyStatus());
        result.put("verifyRemark", user.getVerifyRemark());
        result.put("frozenReason", user.getFrozenReason());
        return result;
    }
}
