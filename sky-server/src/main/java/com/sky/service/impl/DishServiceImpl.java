package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    //新增菜品及其口味
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        //插入一条菜品数据
        dishMapper.insert(dish);

        //获得dishId,用于插入口味表
        Long dishId = dish.getId();

        List<DishFlavor> flavors=dishDTO.getFlavors();
        if(flavors !=null && !flavors.isEmpty()) {
            //为每个flavor赋值对应的dishId
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //批量插入多条口味数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
