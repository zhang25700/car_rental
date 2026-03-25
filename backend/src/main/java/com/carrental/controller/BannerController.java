package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.entity.Banner;
import com.carrental.mapper.BannerMapper;
import com.carrental.security.RoleAllowed;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/banners")
@RoleAllowed("ADMIN")
public class BannerController {
    private static final Set<String> ALLOWED_SUFFIXES = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    @Autowired
    private BannerMapper bannerMapper;

    @GetMapping
    public ApiResponse<List<Banner>> list() {
        return ApiResponse.success(bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
            .orderByAsc(Banner::getSort).orderByDesc(Banner::getId)));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Banner banner) {
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.insert(banner);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.updateById(banner);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return ApiResponse.success();
    }

    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.fail(400, "请选择图片文件");
        }
        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String suffix = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        if (!ALLOWED_SUFFIXES.contains(suffix)) {
            return ApiResponse.fail(400, "仅支持 jpg、png、webp、gif 格式图片");
        }
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "banners");
        Files.createDirectories(uploadDir);
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        Path filePath = uploadDir.resolve(fileName);
        file.transferTo(filePath);
        return ApiResponse.success("/uploads/banners/" + fileName);
    }
}
