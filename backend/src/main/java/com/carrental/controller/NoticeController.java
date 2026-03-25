package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.entity.Notice;
import com.carrental.mapper.NoticeMapper;
import com.carrental.security.RoleAllowed;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    @Autowired
    private NoticeMapper noticeMapper;

    @GetMapping
    public ApiResponse<List<Notice>> list() {
        return ApiResponse.success(noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
            .eq(Notice::getStatus, 1)
            .orderByAsc(Notice::getSort).orderByDesc(Notice::getPublishAt)));
    }

    @RoleAllowed("ADMIN")
    @GetMapping("/admin")
    public ApiResponse<List<Notice>> adminList() {
        return ApiResponse.success(noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
            .orderByAsc(Notice::getSort).orderByDesc(Notice::getId)));
    }

    @RoleAllowed("ADMIN")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Notice notice) {
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        if (notice.getPublishAt() == null) {
            notice.setPublishAt(LocalDateTime.now());
        }
        noticeMapper.insert(notice);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return ApiResponse.success();
    }
}
