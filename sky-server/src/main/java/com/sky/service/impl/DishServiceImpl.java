package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Objects;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealMapper setmealMapper;

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

    //菜品分页查询
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    //菜品批量删除
    @Transactional
    public void deleteDishById(List<Long> dishIds) {
        //判断当前菜品是否处于起售状态，如果是则不能删除
        dishIds.forEach(id ->{
            Dish dish = dishMapper.getDishById(id);
            if(dish != null && Objects.equals(dish.getStatus(), StatusConstant.ENABLE))
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        });
        //判断当前菜品是否关联套餐，如果是则不能删除
        dishIds.forEach(id ->{
            List<Long> setmealIds =setmealMapper.getSetmealIdsByDishId(id);
            if(setmealIds !=null && !setmealIds.isEmpty()){
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
                });

        //dishIds.forEach(id ->{

            //删除菜品
            //dishMapper.deleteById(id);
            //删除菜品所关联的口味
            //dishFlavorMapper.deleteByDishId(id);

        //});

        //根据菜品id集合批量删除菜品数据  delete from dish where id in (...)
        dishMapper.deleteByIds(dishIds);

        //根据菜品id集合批量删除口味数据 delete from dish_flavor where dish_id in (...)
        dishFlavorMapper.deleteByDishIds(dishIds);
    }


    //根据id查询菜品
    public DishVO getDishWithFlavorById(Long id) {
        Dish dish=dishMapper.getDishById(id);
        List<DishFlavor> dishFlavors=dishFlavorMapper.getDishFlavorsByDishId(id);
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    //修改菜品
    @Transactional
    public void updateDishWithFlavor(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.updateDish(dish);
        //删除关联的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //重新批量插入口味数据
        List<DishFlavor> dishFlavors=dishDTO.getFlavors();
        if(dishFlavors !=null && !dishFlavors.isEmpty()){
            dishFlavors.forEach(flavor ->{
                flavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(dishFlavors);
        }
    }

    //更改菜品售卖状态
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.updateDish(dish);
    }

    //根据分类id查询菜品
    public List<Dish> getDishByCategoryId(Integer categoryId) {

        List<Dish> list = dishMapper.getDishByCategoryId(categoryId);

        return list;
    }

}
