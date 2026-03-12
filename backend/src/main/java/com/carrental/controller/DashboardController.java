package com.carrental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.ApiResponse;
import com.carrental.entity.Car;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.mapper.CarMapper;
import com.carrental.mapper.RentalOrderMapper;
import com.carrental.mapper.UserMapper;
import com.carrental.security.RoleAllowed;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private RentalOrderMapper orderMapper;

    @RoleAllowed("ADMIN")
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("carCount", carMapper.selectCount(null));
        data.put("availableCarCount", carMapper.selectCount(new LambdaQueryWrapper<Car>().eq(Car::getStatus, "AVAILABLE")));
        data.put("orderCount", orderMapper.selectCount(null));
        data.put("customerCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "CUSTOMER")));
        BigDecimal income = orderMapper.selectList(new LambdaQueryWrapper<RentalOrder>().eq(RentalOrder::getPaymentStatus, "PAID"))
            .stream()
            .map(RentalOrder::getRentAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("income", income);
        return ApiResponse.success(data);
    }

    @RoleAllowed("ADMIN")
    @GetMapping("/charts")
    public ApiResponse<Map<String, Object>> charts() {
        Map<String, Object> data = new HashMap<>();

        List<Car> cars = carMapper.selectList(null);
        List<RentalOrder> orders = orderMapper.selectList(null);

        List<Map<String, Object>> energyDistribution = cars.stream()
            .collect(Collectors.groupingBy(Car::getEnergyType, Collectors.counting()))
            .entrySet()
            .stream()
            .map(entry -> buildChartItem(entry.getKey(), entry.getValue()))
            .toList();

        List<Map<String, Object>> transmissionDistribution = cars.stream()
            .collect(Collectors.groupingBy(Car::getTransmission, Collectors.counting()))
            .entrySet()
            .stream()
            .map(entry -> buildChartItem(entry.getKey(), entry.getValue()))
            .toList();

        List<Map<String, Object>> orderStatusDistribution = orders.stream()
            .collect(Collectors.groupingBy(RentalOrder::getOrderStatus, Collectors.counting()))
            .entrySet()
            .stream()
            .map(entry -> buildChartItem(entry.getKey(), entry.getValue()))
            .toList();

        List<Map<String, Object>> stationDistribution = new ArrayList<>();
        cars.stream()
            .collect(Collectors.groupingBy(Car::getStationId, Collectors.counting()))
            .forEach((stationId, count) -> {
                Map<String, Object> item = new HashMap<>();
                item.put("stationId", stationId);
                item.put("value", count);
                stationDistribution.add(item);
            });

        List<RentalOrder> paidOrders = orders.stream()
            .filter(order -> "PAID".equals(order.getPaymentStatus()))
            .toList();

        data.put("energyDistribution", energyDistribution);
        data.put("transmissionDistribution", transmissionDistribution);
        data.put("orderStatusDistribution", orderStatusDistribution);
        data.put("stationDistribution", stationDistribution);
        data.put("incomeTrendByDay", aggregateIncomeTrend(paidOrders, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        data.put("incomeTrendByMonth", aggregateIncomeTrend(paidOrders, DateTimeFormatter.ofPattern("yyyy-MM")));
        data.put("incomeTrendByYear", aggregateIncomeTrend(paidOrders, DateTimeFormatter.ofPattern("yyyy")));
        return ApiResponse.success(data);
    }

    private Map<String, Object> buildChartItem(String name, Long value) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private List<Map<String, Object>> aggregateIncomeTrend(List<RentalOrder> orders, DateTimeFormatter formatter) {
        return new LinkedHashMap<>(orders.stream()
            .collect(Collectors.groupingBy(
                order -> formatOrderTime(order.getUpdatedAt(), formatter),
                Collectors.mapping(RentalOrder::getRentAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            )))
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("label", entry.getKey());
                item.put("value", entry.getValue());
                return item;
            })
            .toList();
    }

    private String formatOrderTime(LocalDateTime time, DateTimeFormatter formatter) {
        LocalDateTime actualTime = time == null ? LocalDateTime.now() : time;
        return actualTime.format(formatter);
    }
}
