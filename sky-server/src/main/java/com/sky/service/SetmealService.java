package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    //新增套餐
    void saveSetmealWithDish(SetmealDTO setmealDTO);
    //套餐分页查询
    PageResult setmealPageQuery(SetmealPageQueryDTO setmealPageQueryDTO);
    //批量删除套餐
    void deleteBatch(List<Long> ids);
    //根据id查询套餐以及关联菜品数据
    SetmealVO getSetmealByIdWithDish(Long id);
    //修改套餐
    void updateSetmealWithDish(SetmealDTO setmealDTO);
    //起售停售套餐
    void startOrStop(Integer status, Long id);

    //条件查询（C端商品浏览）
    List<Setmeal> list(Setmeal setmeal);

    //根据套餐id查询包含的菜品（C端商品浏览）
    List<DishItemVO> getDishItemById(Long id);
}
