package com.carrental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carrental.common.BusinessException;
import com.carrental.dto.OrderCreateRequest;
import com.carrental.entity.Car;
import com.carrental.entity.RentalOrder;
import com.carrental.entity.User;
import com.carrental.mapper.CarMapper;
import com.carrental.mapper.RentalOrderMapper;
import com.carrental.mapper.UserMapper;
import com.carrental.service.IOrderService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    private final RentalOrderMapper orderMapper;
    private final CarMapper carMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public RentalOrder create(Long userId, OrderCreateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!"APPROVED".equals(user.getVerifyStatus())) {
            throw new BusinessException(400, "证件未审核通过，暂不可下单");
        }
        Car car = carMapper.selectById(request.getCarId());
        if (car == null) {
            throw new BusinessException(404, "车辆不存在");
        }
        if ("MAINTENANCE".equals(car.getStatus()) || "DISABLED".equals(car.getStatus())) {
            throw new BusinessException(400, "当前车辆不可租赁");
        }
        long rentalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (rentalDays <= 0) {
            throw new BusinessException(400, "还车日期不能早于取车日期");
        }
        boolean hasConflict = orderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
            .eq(RentalOrder::getCarId, request.getCarId())
            .in(RentalOrder::getOrderStatus, "BOOKED", "ONGOING")
            .le(RentalOrder::getStartDate, request.getEndDate())
            .ge(RentalOrder::getEndDate, request.getStartDate())) > 0;
        if (hasConflict) {
            throw new BusinessException(400, "所选时间段车辆已被预订");
        }

        RentalOrder order = new RentalOrder();
        order.setOrderNo("CR" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        order.setUserId(userId);
        order.setCarId(request.getCarId());
        order.setStationId(request.getStationId());
        order.setReturnStationId(request.getReturnStationId());
        order.setStartDate(request.getStartDate());
        order.setEndDate(request.getEndDate());
        order.setRentalDays((int) rentalDays);
        order.setRentAmount(car.getDailyPrice().multiply(BigDecimal.valueOf(rentalDays)));
        order.setDepositAmount(car.getDeposit());
        order.setExtraFee(BigDecimal.ZERO);
        order.setSettlementAmount(order.getRentAmount().add(order.getDepositAmount()));
        order.setTotalAmount(order.getRentAmount().add(order.getDepositAmount()));
        order.setOrderStatus("BOOKED");
        order.setPaymentStatus("UNPAID");
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPickupAddress(request.getPickupAddress());
        order.setPickupLongitude(request.getPickupLongitude());
        order.setPickupLatitude(request.getPickupLatitude());
        order.setReturnAddress(request.getReturnAddress());
        order.setReturnLongitude(request.getReturnLongitude());
        order.setReturnLatitude(request.getReturnLatitude());
        order.setRemark(request.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional
    public void pay(Long orderId, Long userId, boolean adminAction) {
        RentalOrder order = getOwnedOrder(orderId, userId, adminAction);
        if (!"UNPAID".equals(order.getPaymentStatus())) {
            throw new BusinessException(400, "订单已支付");
        }
        order.setPaymentStatus("PAID");
        order.setOrderStatus("ONGOING");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        refreshCarStatus(order.getCarId());
    }

    @Override
    @Transactional
    public void cancel(Long orderId, Long userId, boolean adminAction) {
        RentalOrder order = getOwnedOrder(orderId, userId, adminAction);
        if (!List.of("BOOKED", "ONGOING").contains(order.getOrderStatus())) {
            throw new BusinessException(400, "当前订单不可取消");
        }
        order.setOrderStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        refreshCarStatus(order.getCarId());
    }

    @Override
    @Transactional
    public void complete(Long orderId, Long userId, boolean adminAction) {
        RentalOrder order = getOwnedOrder(orderId, userId, adminAction);
        Car car = carMapper.selectById(order.getCarId());
        BigDecimal extraFee = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();
        if (today.isAfter(order.getEndDate()) && car != null) {
            long overdueDays = ChronoUnit.DAYS.between(order.getEndDate(), today);
            extraFee = car.getDailyPrice().multiply(BigDecimal.valueOf(overdueDays));
        }
        order.setExtraFee(extraFee);
        order.setSettlementAmount(order.getTotalAmount().add(extraFee));
        order.setOrderStatus("COMPLETED");
        order.setReturnedAt(LocalDateTime.now());
        order.setSettledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        refreshCarStatus(order.getCarId());
    }

    @Override
    public List<RentalOrder> listByUser(Long userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<RentalOrder>().eq(RentalOrder::getUserId, userId).orderByDesc(RentalOrder::getId));
    }

    @Override
    public List<RentalOrder> listAll() {
        return orderMapper.selectList(new LambdaQueryWrapper<RentalOrder>().orderByDesc(RentalOrder::getId));
    }

    @Override
    public RentalOrder getOwnedOrderDetail(Long orderId, Long userId, boolean adminAction) {
        return getOwnedOrder(orderId, userId, adminAction);
    }

    @Override
    @Transactional
    public void markPaidByOrderNo(String orderNo, String tradeNo) {
        RentalOrder order = orderMapper.selectOne(new LambdaQueryWrapper<RentalOrder>()
            .eq(RentalOrder::getOrderNo, orderNo)
            .last("limit 1"));
        if (order == null || "PAID".equals(order.getPaymentStatus())) {
            return;
        }
        order.setPaymentStatus("PAID");
        order.setOrderStatus("ONGOING");
        order.setTradeNo(tradeNo);
        if (order.getPaymentMethod() == null) {
            order.setPaymentMethod("ALIPAY");
        }
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        refreshCarStatus(order.getCarId());
    }

    private RentalOrder getOwnedOrder(Long orderId, Long userId, boolean adminAction) {
        RentalOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!adminAction && !order.getUserId().equals(userId)) {
            throw new BusinessException(403, "不能操作他人的订单");
        }
        return order;
    }

    private void refreshCarStatus(Long carId) {
        Car car = carMapper.selectById(carId);
        if (car == null || "MAINTENANCE".equals(car.getStatus()) || "DISABLED".equals(car.getStatus())) {
            return;
        }
        LocalDate today = LocalDate.now();
        boolean rentedToday = orderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
            .eq(RentalOrder::getCarId, carId)
            .eq(RentalOrder::getOrderStatus, "ONGOING")
            .le(RentalOrder::getStartDate, today)
            .ge(RentalOrder::getEndDate, today)) > 0;
        car.setStatus(rentedToday ? "RENTED" : "AVAILABLE");
        car.setUpdatedAt(LocalDateTime.now());
        carMapper.updateById(car);
    }
}
