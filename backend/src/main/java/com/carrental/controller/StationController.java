package com.carrental.controller;

import com.carrental.common.ApiResponse;
import com.carrental.entity.PickupStation;
import com.carrental.mapper.PickupStationMapper;
import com.carrental.security.RoleAllowed;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stations")
public class StationController {
    @Autowired
    private PickupStationMapper stationMapper;

    @GetMapping
    public ApiResponse<List<PickupStation>> list() {
        return ApiResponse.success(stationMapper.selectList(null));
    }

    @RoleAllowed("ADMIN")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody PickupStation station) {
        station.setCreatedAt(LocalDateTime.now());
        station.setUpdatedAt(LocalDateTime.now());
        stationMapper.insert(station);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody PickupStation station) {
        station.setId(id);
        station.setUpdatedAt(LocalDateTime.now());
        stationMapper.updateById(station);
        return ApiResponse.success();
    }

    @RoleAllowed("ADMIN")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        stationMapper.deleteById(id);
        return ApiResponse.success();
    }
}
