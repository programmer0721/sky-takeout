package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    //新增购物车
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //一个用户只能查看自己的购物车
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //判断当前新增菜品或套餐是否在该用户购物车里
        List<ShoppingCart>  shoppingCartList = shoppingCartMapper.list(shoppingCart);
        //已经有该菜品或者套餐了，更新
        if(shoppingCartList != null && !shoppingCartList.isEmpty()){
            //获取这个菜品或者套餐的数据，更新数量
            shoppingCart = shoppingCartList.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(shoppingCart);
        }
        else{
            //新数据，没买过，插入
            //判断是菜品还是套餐
            if(shoppingCartDTO.getDishId() != null){
                //是菜品
                Dish dish = dishMapper.getDishById(shoppingCartDTO.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }
            else{
                //是套餐
                Setmeal setmeal = setmealMapper.getSetmealById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    //查看购物车
    public List<ShoppingCart> showShoppingCart() {
        return   shoppingCartMapper.list(ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build());
    }

    //清空当前用户的购物车
    public void cleanShoppingCartByUserId(Long currentId) {
        shoppingCartMapper.deleteShoppingCartByUserId(currentId);
    }

    //删除购物车的一个数据
    public void deleteOneFromShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //一个用户只能查看自己的购物车
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //判断当前要删的商品是否在该用户购物车里
        List<ShoppingCart>  shoppingCartList = shoppingCartMapper.list(shoppingCart);
        //要删的这个商品存在
        if(shoppingCartList != null && !shoppingCartList.isEmpty()){
            shoppingCart = shoppingCartList.get(0);
            Integer num = shoppingCart.getNumber();
            //数量只有一，直接删除该条购物车数据
            if(num == 1){
                shoppingCartMapper.deleteShoppingCartById(shoppingCart.getId());
            }
            //数量有多个，数量减一
            else{
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }
        }
    }
}
