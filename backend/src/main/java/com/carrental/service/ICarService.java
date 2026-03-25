package com.carrental.service;

import com.carrental.entity.Car;
import java.util.List;

public interface ICarService {
    List<Car> list(String keyword, String brand, String typeName, String transmission, String energyType,
                   Boolean availableOnly, Double minPrice, Double maxPrice, String sortBy);
}
