package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")

public class DishController {

    @Autowired
    private DishService dishService;

    //新增菜品
    @ApiOperation("新增菜品")
    @PostMapping
    @CacheEvict(cacheNames = "dishCache", key = "#dishDTO.categoryId")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    //菜品分页查询
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}",dishPageQueryDTO);
        PageResult pageResult=dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    //菜品批量删除
    @ApiOperation("菜品批量删除")
    @DeleteMapping
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result deleteBatch(@RequestParam("ids") List<Long> dishIds){
        dishService.deleteDishById(dishIds);
        return Result.success();
    }

    //根据id查询菜品
    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getDishWithFlavorById(@PathVariable Long id){
        log.info("根据id查询菜品:{}",id);
        DishVO dishVO = dishService.getDishWithFlavorById(id);
        return Result.success(dishVO);
    }

    //修改菜品
    @ApiOperation("修改菜品")
    @PutMapping
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result updateDishWithFlavor(@RequestBody DishDTO dishDTO){
        dishService.updateDishWithFlavor(dishDTO);
        return Result.success();
    }

    //更改菜品售卖状态
    @ApiOperation("更改菜品售卖状态")
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status,Long id){
        dishService.startOrStop(status,id);
        return Result.success();
    }

    //根据分类id查询菜品
    @ApiOperation("根据分类id查询菜品")
    @GetMapping("/list")
    public Result<List<Dish>> getDishByCategoryId(Integer categoryId){
        List<Dish> list=dishService.getDishByCategoryId(categoryId);
        return Result.success(list);
    }
}
