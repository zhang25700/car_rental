package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.entity.CarBrand;
import com.carrental.mapper.CarBrandMapper;
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
@RequestMapping("/api/brands")
public class CarBrandController {
    @Autowired
    private CarBrandMapper carBrandMapper;

    @GetMapping
    public ApiResponse<List<CarBrand>> list() {
        return ApiResponse.success(carBrandMapper.selectList(new LambdaQueryWrapper<CarBrand>()
            .eq(CarBrand::getStatus, 1).orderByAsc(CarBrand::getSort).orderByAsc(CarBrand::getId)));
    }

    @RoleAllowed("ADMIN")
    @GetMapping("/admin")
    public ApiResponse<List<CarBrand>> adminList() {
        return ApiResponse.success(carBrandMapper.selectList(new LambdaQueryWrapper<CarBrand>()
            .orderByAsc(CarBrand::getSort).orderByAsc(CarBrand::getId)));
    }

    @RoleAllowed("ADMIN")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody CarBrand brand) {
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());
        carBrandMapper.insert(brand);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CarBrand brand) {
        brand.setId(id);
        brand.setUpdatedAt(LocalDateTime.now());
        carBrandMapper.updateById(brand);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        carBrandMapper.deleteById(id);
        return ApiResponse.success();
    }
}
