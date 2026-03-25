package com.carrental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("rental_order")
public class RentalOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long carId;
    private Long stationId;
    private Long returnStationId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer rentalDays;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private BigDecimal extraFee;
    private BigDecimal settlementAmount;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;
    private String pickupAddress;
    private String pickupLongitude;
    private String pickupLatitude;
    private String returnAddress;
    private String returnLongitude;
    private String returnLatitude;
    private String tradeNo;
    private String remark;
    private LocalDateTime returnedAt;
    private LocalDateTime settledAt;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
