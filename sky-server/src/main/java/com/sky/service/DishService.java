package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

    //新增菜品及其口味
public interface DishService {
    public void saveWithFlavor(DishDTO dishDTO);
    //菜品分页查询
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);
    //菜品批量删除
    void deleteDishById(List<Long> dishId);
    //根据id查询菜品
    DishVO getDishWithFlavorById(Long id);
    //修改菜品
    void updateDishWithFlavor(DishDTO dishDTO);
    //更改菜品售卖状态
    void startOrStop(Integer status, Long id);
    //根据分类id查询菜品
    List<Dish> getDishByCategoryId(Integer categoryId);
    }
