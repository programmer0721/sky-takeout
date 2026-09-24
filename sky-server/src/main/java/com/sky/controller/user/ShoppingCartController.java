package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "购物车相关接口")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    //新增购物车
    @ApiOperation("新增购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("新增购物车：{}",shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    //查看购物车
    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> showShoppingCart(){
        List<ShoppingCart> list = shoppingCartService.showShoppingCart();
        return Result.success(list);
    }

    //清空购物车
    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result cleanShoppingCart(){
        shoppingCartService.cleanShoppingCartByUserId(BaseContext.getCurrentId());
        return Result.success();
    }

    //删除购物车的一个数据
    @ApiOperation("删除购物车的一个数据")
    @PostMapping("/sub")
    public Result deleteOneFromShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.deleteOneFromShoppingCart(shoppingCartDTO);
        return Result.success();
    }
}
