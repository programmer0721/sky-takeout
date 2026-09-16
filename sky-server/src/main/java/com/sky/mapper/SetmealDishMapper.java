package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    //批量插入菜品数据
    void insertBatch(List<SetmealDish> setmealDishes);

    //根据套餐id集合批量删除套餐菜品表数据
    void deleteBySetmealIds(List<Long> ids);

    //根据套餐id查询套餐和套餐菜品关系
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getDishesBySetmealId(Long id);

    //根据套餐id删除套餐菜品表数据
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
}
