package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.entity.Banner;
import com.carrental.entity.Car;
import com.carrental.entity.CarBrand;
import com.carrental.entity.CarType;
import com.carrental.entity.Notice;
import com.carrental.mapper.BannerMapper;
import com.carrental.mapper.CarBrandMapper;
import com.carrental.mapper.CarMapper;
import com.carrental.mapper.CarTypeMapper;
import com.carrental.mapper.NoticeMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PortalController {
    @Autowired
    private BannerMapper bannerMapper;
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private CarMapper carMapper;
    @Autowired
    private CarBrandMapper carBrandMapper;
    @Autowired
    private CarTypeMapper carTypeMapper;

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("banners", bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
            .eq(Banner::getStatus, 1)
            .orderByAsc(Banner::getSort).orderByDesc(Banner::getId)));
        data.put("notices", noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
            .eq(Notice::getStatus, 1)
            .orderByAsc(Notice::getSort).orderByDesc(Notice::getId)));
        data.put("hotCars", carMapper.selectList(new LambdaQueryWrapper<Car>()
            .eq(Car::getHotFlag, 1)
            .orderByDesc(Car::getId)
            .last("limit 6")));
        data.put("brands", carBrandMapper.selectList(new LambdaQueryWrapper<CarBrand>()
            .eq(CarBrand::getStatus, 1).orderByAsc(CarBrand::getSort).orderByAsc(CarBrand::getId)));
        data.put("types", carTypeMapper.selectList(new LambdaQueryWrapper<CarType>()
            .eq(CarType::getStatus, 1).orderByAsc(CarType::getSort).orderByAsc(CarType::getId)));
        return ApiResponse.success(data);
    }
}
