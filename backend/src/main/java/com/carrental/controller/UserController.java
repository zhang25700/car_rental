package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.entity.User;
import com.carrental.mapper.UserMapper;
import com.carrental.security.AuthContext;
import com.carrental.security.RoleAllowed;
import com.carrental.util.PasswordUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Set<String> ALLOWED_SUFFIXES = Set.of(".jpg", ".jpeg", ".png", ".webp");

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/profile")
    public ApiResponse<User> profile() {
        User user = userMapper.selectById(AuthContext.get().getUserId());
        if (user != null) {
            user.setPassword(null);
        }
        return ApiResponse.success(user);
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestBody User user) {
        User existing = userMapper.selectById(AuthContext.get().getUserId());
        existing.setRealName(user.getRealName());
        existing.setPhone(user.getPhone());
        existing.setEmail(user.getEmail());
        existing.setIdCardNo(user.getIdCardNo());
        existing.setIdCardFront(user.getIdCardFront());
        existing.setIdCardBack(user.getIdCardBack());
        existing.setVerifyStatus("PENDING");
        existing.setVerifyRemark("资料已更新，等待管理员复核");
        existing.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(existing);
        return ApiResponse.success();
    }

    @PostMapping("/profile/upload")
    public ApiResponse<String> uploadProfileImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.fail(400, "请选择图片文件");
        }
        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String suffix = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        if (!ALLOWED_SUFFIXES.contains(suffix)) {
            return ApiResponse.fail(400, "仅支持 jpg、png、webp 格式图片");
        }
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "idcards");
        Files.createDirectories(uploadDir);
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        Path filePath = uploadDir.resolve(fileName);
        file.transferTo(filePath);
        return ApiResponse.success("/uploads/idcards/" + fileName);
    }

    @RoleAllowed("ADMIN")
    @GetMapping
    public ApiResponse<List<User>> list(@RequestParam(required = false) String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getId);
        if (role != null && !role.isBlank()) {
            wrapper.eq(User::getRole, role);
        }
        List<User> users = userMapper.selectList(wrapper);
        users.forEach(item -> item.setPassword(null));
        return ApiResponse.success(users);
    }

    @RoleAllowed("ADMIN")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody User user) {
        user.setPassword(PasswordUtils.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        if (user.getVerifyStatus() == null) {
            user.setVerifyStatus("PENDING");
        }
        userMapper.insert(user);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody User user) {
        User existing = userMapper.selectById(id);
        existing.setUsername(user.getUsername());
        existing.setRealName(user.getRealName());
        existing.setPhone(user.getPhone());
        existing.setEmail(user.getEmail());
        existing.setRole(user.getRole());
        existing.setStatus(user.getStatus());
        existing.setIdCardNo(user.getIdCardNo());
        existing.setIdCardFront(user.getIdCardFront());
        existing.setIdCardBack(user.getIdCardBack());
        existing.setVerifyStatus(user.getVerifyStatus());
        existing.setVerifyRemark(user.getVerifyRemark());
        existing.setFrozenReason(user.getFrozenReason());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(PasswordUtils.encode(user.getPassword()));
        }
        existing.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(existing);
        return ApiResponse.success();
    }
}
