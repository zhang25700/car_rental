package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.entity.Car;
import com.carrental.mapper.CarMapper;
import com.carrental.security.RoleAllowed;
import com.carrental.service.ICarService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    @Autowired
    private ICarService carService;

    @Autowired
    private CarMapper carMapper;

    @GetMapping
    public ApiResponse<List<Car>> list(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String brand,
                                       @RequestParam(required = false) String transmission,
                                       @RequestParam(required = false) String energyType,
                                       @RequestParam(required = false) Boolean availableOnly) {
        return ApiResponse.success(carService.list(keyword, brand, transmission, energyType, availableOnly));
    }

    @GetMapping("/{id}")
    public ApiResponse<Car> detail(@PathVariable Long id) {
        return ApiResponse.success(carMapper.selectById(id));
    }

    @GetMapping("/brands")
    public ApiResponse<List<String>> brands() {
        return ApiResponse.success(carMapper.selectList(new LambdaQueryWrapper<Car>().select(Car::getBrand).groupBy(Car::getBrand))
            .stream()
            .map(Car::getBrand)
            .toList());
    }

    @RoleAllowed("ADMIN")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Car car) {
        car.setCreatedAt(LocalDateTime.now());
        car.setUpdatedAt(LocalDateTime.now());
        carMapper.insert(car);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Car car) {
        car.setId(id);
        car.setUpdatedAt(LocalDateTime.now());
        carMapper.updateById(car);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        carMapper.deleteById(id);
        return ApiResponse.success();
    }
}
