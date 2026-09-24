package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    //新增购物车
    void add(ShoppingCartDTO shoppingCartDTO);
    //查看购物车
    List<ShoppingCart> showShoppingCart();
    //清空当前用户的购物车
    void cleanShoppingCartByUserId(Long currentId);
    //删除购物车的一个数据
    void deleteOneFromShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
