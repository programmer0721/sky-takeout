package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class SetmealServiceImpl implements SetmealService {

     @Autowired
     private SetmealMapper setmealMapper;

     @Autowired
     private SetmealDishMapper setmealDishMapper;

    //新增套餐
    @Transactional
    public void saveSetmealWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.saveSetmeal(setmeal);
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }

    //套餐分页查询
    public PageResult setmealPageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.setmealPageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    //删除套餐
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(setmealId ->{
            Setmeal setmeal = setmealMapper.getSetmealById(setmealId);
            if(setmeal != null && Objects.equals(setmeal.getStatus(), StatusConstant.ENABLE))
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        });
        //根据套餐id集合批量删除套餐数据  delete from setmeal where id in (...)
        setmealMapper.deleteByIds(ids);

        //根据套餐id集合批量删除套餐菜品表数据 delete from setmeal_dish where setmeal_id in (...)
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    //根据id查询套餐以及关联菜品数据
    public SetmealVO getSetmealByIdWithDish(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        Setmeal setmeal = setmealMapper.getSetmealById(id);
        BeanUtils.copyProperties(setmeal,setmealVO);
        List<SetmealDish> dishes = setmealDishMapper.getDishesBySetmealId(id);
        setmealVO.setSetmealDishes(dishes);
        return setmealVO;
    }

    //修改套餐
    @Transactional
    public void updateSetmealWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.updateSetmeal(setmeal);
        Long setmealId = setmeal.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    //起售停售套餐
    public void startOrStop(Integer status, Long id) {
        //起售时校验套餐内是否包含未启售的菜品，包含则不允许起售；停售无需校验
        if (Objects.equals(status, StatusConstant.ENABLE)) {
            Integer count = setmealMapper.countDisabledDishBySetmealId(id);
            if (count != null && count > 0) {
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.updateSetmeal(setmeal);
    }

    //条件查询（C端商品浏览）
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    //根据套餐id查询包含的菜品（C端商品浏览）
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
