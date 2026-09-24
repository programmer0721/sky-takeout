package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    //根据传入条件查询购物车数据
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    //更新购物车数量
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    //插入购物车数据
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            " values (#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{image},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    //根据用户Id删除购物车数据
    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void deleteShoppingCartByUserId(Long currentId);

    //根据Id删除购物车数据
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteShoppingCartById(Long id);
}
