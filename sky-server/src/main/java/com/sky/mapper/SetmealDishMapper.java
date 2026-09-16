package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    //批量插入菜品数据
    void insertBatch(List<SetmealDish> setmealDishes);
}
