package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    //新增套餐
    @ApiOperation("新增套餐")
    @PostMapping
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐：{}",setmealDTO);
         setmealService.saveSetmealWithDish(setmealDTO);
         return Result.success();
    }

    //套餐分页查询
    @ApiOperation("套餐分页查询")
    @GetMapping("/page")
    public Result<PageResult> setmealPageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        PageResult pageResult = setmealService.setmealPageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    //批量删除套餐
    @ApiOperation("批量删除套餐")
    @DeleteMapping
    public Result deleteSetmealById(@RequestParam List<Long> ids) {
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    //根据id查询套餐以及关联菜品数据
    @ApiOperation("根据id查询套餐以及关联菜品数据")
    @GetMapping("/{id}")
    public Result<SetmealVO> getSetmealById(@PathVariable Long id){
        SetmealVO setmealVO = setmealService.getSetmealByIdWithDish(id);
        return Result.success(setmealVO);
    }

    //修改套餐
    @ApiOperation("修改套餐")
    @PutMapping
    public Result updateSetmealWithDish(@RequestBody SetmealDTO setmealDTO){
        setmealService.updateSetmealWithDish(setmealDTO);
        return Result.success();
    }

    //起售停售套餐
    @ApiOperation("起售停售套餐")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,Long id){
        setmealService.startOrStop(status,id);
        return Result.success();
    }
}
