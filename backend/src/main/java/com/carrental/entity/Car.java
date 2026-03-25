package com.carrental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("car")
public class Car {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String brand;
    private String typeName;
    private String model;
    private String plateNumber;
    private String color;
    private String transmission;
    private String energyType;
    private Integer seatCount;
    private BigDecimal dailyPrice;
    private BigDecimal deposit;
    private String status;
    private Long stationId;
    private Integer hotFlag;
    private String coverImage;
    private String description;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
