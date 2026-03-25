package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.entity.Feedback;
import com.carrental.mapper.FeedbackMapper;
import com.carrental.security.AuthContext;
import com.carrental.security.RoleAllowed;
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
@RequestMapping("/api/feedbacks")
public class FeedbackController {
    @Autowired
    private FeedbackMapper feedbackMapper;

    @GetMapping("/my")
    public ApiResponse<List<Feedback>> myList() {
        return ApiResponse.success(feedbackMapper.selectList(new LambdaQueryWrapper<Feedback>()
            .eq(Feedback::getUserId, AuthContext.get().getUserId())
            .orderByDesc(Feedback::getId)));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Feedback feedback) {
        feedback.setUserId(AuthContext.get().getUserId());
        feedback.setStatus("PENDING");
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @GetMapping
    public ApiResponse<List<Feedback>> list() {
        return ApiResponse.success(feedbackMapper.selectList(new LambdaQueryWrapper<Feedback>()
            .orderByDesc(Feedback::getId)));
    }

    @RoleAllowed("ADMIN")
    @PutMapping("/{id}/reply")
    public ApiResponse<Void> reply(@PathVariable Long id, @RequestBody Feedback feedback) {
        Feedback existing = feedbackMapper.selectById(id);
        existing.setReplyContent(feedback.getReplyContent());
        existing.setStatus("REPLIED");
        existing.setReplyAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        feedbackMapper.updateById(existing);
        return ApiResponse.success();
    }
}
