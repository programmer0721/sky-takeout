package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

//新增菜品及其口味
public interface DishService {
    public void saveWithFlavor(DishDTO dishDTO);
//菜品分页查询
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);
}
