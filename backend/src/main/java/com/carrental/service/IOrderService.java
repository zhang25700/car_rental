package com.carrental.service;

import com.carrental.dto.OrderCreateRequest;
import com.carrental.entity.RentalOrder;
import java.util.List;

public interface IOrderService {
    RentalOrder create(Long userId, OrderCreateRequest request);

    void pay(Long orderId, Long userId, boolean adminAction);

    void cancel(Long orderId, Long userId, boolean adminAction);

    void complete(Long orderId, Long userId, boolean adminAction);

    List<RentalOrder> listByUser(Long userId);

    List<RentalOrder> listAll();

    RentalOrder getOwnedOrderDetail(Long orderId, Long userId, boolean adminAction);

    void markPaidByOrderNo(String orderNo, String tradeNo);
}
