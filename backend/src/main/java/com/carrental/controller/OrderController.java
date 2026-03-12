package com.carrental.controller;

import com.carrental.common.ApiResponse;
import com.carrental.dto.OrderCreateRequest;
import com.carrental.entity.RentalOrder;
import com.carrental.security.AuthContext;
import com.carrental.security.RoleAllowed;
import com.carrental.service.IOrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @GetMapping("/my")
    public ApiResponse<List<RentalOrder>> myOrders() {
        return ApiResponse.success(orderService.listByUser(AuthContext.get().getUserId()));
    }

    @RoleAllowed("ADMIN")
    @GetMapping
    public ApiResponse<List<RentalOrder>> allOrders() {
        return ApiResponse.success(orderService.listAll());
    }

    @PostMapping
    public ApiResponse<RentalOrder> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(orderService.create(AuthContext.get().getUserId(), request));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<Void> pay(@PathVariable Long id) {
        orderService.pay(id, AuthContext.get().getUserId(), false);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id, AuthContext.get().getUserId(), false);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/return")
    public ApiResponse<Void> complete(@PathVariable Long id) {
        orderService.complete(id, AuthContext.get().getUserId(), false);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @PostMapping("/{id}/admin/pay")
    public ApiResponse<Void> adminPay(@PathVariable Long id) {
        orderService.pay(id, AuthContext.get().getUserId(), true);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @PostMapping("/{id}/admin/cancel")
    public ApiResponse<Void> adminCancel(@PathVariable Long id) {
        orderService.cancel(id, AuthContext.get().getUserId(), true);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @PostMapping("/{id}/admin/return")
    public ApiResponse<Void> adminReturn(@PathVariable Long id) {
        orderService.complete(id, AuthContext.get().getUserId(), true);
        return ApiResponse.success();
    }
}
