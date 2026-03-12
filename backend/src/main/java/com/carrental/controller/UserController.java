package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.entity.User;
import com.carrental.mapper.UserMapper;
import com.carrental.security.RoleAllowed;
import com.carrental.util.PasswordUtils;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RoleAllowed("ADMIN")
public class UserController {
    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public ApiResponse<List<User>> list() {
        return ApiResponse.success(userMapper.selectList(new LambdaQueryWrapper<User>().orderByDesc(User::getId)));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody User user) {
        user.setPassword(PasswordUtils.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(PasswordUtils.encode(user.getPassword()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.success();
    }
}
