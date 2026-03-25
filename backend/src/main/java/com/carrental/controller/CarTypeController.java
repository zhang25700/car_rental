package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.entity.CarType;
import com.carrental.mapper.CarTypeMapper;
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
@RequestMapping("/api/car-types")
public class CarTypeController {
    @Autowired
    private CarTypeMapper carTypeMapper;

    @GetMapping
    public ApiResponse<List<CarType>> list() {
        return ApiResponse.success(carTypeMapper.selectList(new LambdaQueryWrapper<CarType>()
            .eq(CarType::getStatus, 1).orderByAsc(CarType::getSort).orderByAsc(CarType::getId)));
    }

    @RoleAllowed("ADMIN")
    @GetMapping("/admin")
    public ApiResponse<List<CarType>> adminList() {
        return ApiResponse.success(carTypeMapper.selectList(new LambdaQueryWrapper<CarType>()
            .orderByAsc(CarType::getSort).orderByAsc(CarType::getId)));
    }

    @RoleAllowed("ADMIN")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody CarType carType) {
        carType.setCreatedAt(LocalDateTime.now());
        carType.setUpdatedAt(LocalDateTime.now());
        carTypeMapper.insert(carType);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CarType carType) {
        carType.setId(id);
        carType.setUpdatedAt(LocalDateTime.now());
        carTypeMapper.updateById(carType);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        carTypeMapper.deleteById(id);
        return ApiResponse.success();
    }
}
