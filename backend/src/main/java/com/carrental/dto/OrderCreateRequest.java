package com.carrental.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class OrderCreateRequest {
    @NotNull
    private Long carId;
    @NotNull
    private Long stationId;
    @NotNull
    @FutureOrPresent
    private LocalDate startDate;
    @NotNull
    @FutureOrPresent
    private LocalDate endDate;
    private String pickupAddress;
    private String pickupLongitude;
    private String pickupLatitude;
    private String remark;
}
