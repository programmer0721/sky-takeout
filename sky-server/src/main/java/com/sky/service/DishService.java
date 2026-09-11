package com.sky.service;

import com.sky.dto.DishDTO;

//新增菜品及其口味
public interface DishService {
    public void saveWithFlavor(DishDTO dishDTO);
}
