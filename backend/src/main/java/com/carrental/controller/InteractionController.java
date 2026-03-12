package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.dto.ReviewCreateRequest;
import com.carrental.entity.Favorite;
import com.carrental.entity.Review;
import com.carrental.mapper.FavoriteMapper;
import com.carrental.mapper.ReviewMapper;
import com.carrental.security.AuthContext;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {
    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @GetMapping("/favorites")
    public ApiResponse<List<Favorite>> favorites() {
        return ApiResponse.success(favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, AuthContext.get().getUserId())
            .orderByDesc(Favorite::getId)));
    }

    @PostMapping("/favorites/{carId}")
    public ApiResponse<Void> favorite(@PathVariable Long carId) {
        Long userId = AuthContext.get().getUserId();
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getCarId, carId));
        if (count == 0) {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setCarId(carId);
            favorite.setCreatedAt(LocalDateTime.now());
            favoriteMapper.insert(favorite);
        }
        return ApiResponse.success();
    }

    @DeleteMapping("/favorites/{carId}")
    public ApiResponse<Void> unfavorite(@PathVariable Long carId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, AuthContext.get().getUserId())
            .eq(Favorite::getCarId, carId));
        return ApiResponse.success();
    }

    @GetMapping("/reviews/{carId}")
    public ApiResponse<List<Review>> reviews(@PathVariable Long carId) {
        return ApiResponse.success(reviewMapper.selectList(new LambdaQueryWrapper<Review>()
            .eq(Review::getCarId, carId)
            .orderByDesc(Review::getId)));
    }

    @PostMapping("/reviews")
    public ApiResponse<Void> review(@Valid @RequestBody ReviewCreateRequest request) {
        Review review = new Review();
        review.setUserId(AuthContext.get().getUserId());
        review.setCarId(request.getCarId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        return ApiResponse.success();
    }
}
