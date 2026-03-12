package com.carrental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.entity.Car;
import com.carrental.mapper.CarMapper;
import com.carrental.service.ICarService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements ICarService {
    private final CarMapper carMapper;

    @Override
    public List<Car> list(String keyword, String brand, String transmission, String energyType, Boolean availableOnly) {
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Car::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Car::getBrand, keyword).or().like(Car::getModel, keyword).or().like(Car::getPlateNumber, keyword));
        }
        if (StringUtils.hasText(brand)) {
            wrapper.eq(Car::getBrand, brand);
        }
        if (StringUtils.hasText(transmission)) {
            wrapper.eq(Car::getTransmission, transmission);
        }
        if (StringUtils.hasText(energyType)) {
            wrapper.eq(Car::getEnergyType, energyType);
        }
        if (Boolean.TRUE.equals(availableOnly)) {
            wrapper.eq(Car::getStatus, "AVAILABLE");
        }
        return carMapper.selectList(wrapper);
    }
}
