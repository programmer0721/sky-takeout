package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    //根据菜品id查询关联的套餐id
    List<Long> getSetmealIdsByDishId(Long id);

    //插入一条套餐数据
    @AutoFill(value = OperationType.INSERT)
    void saveSetmeal(Setmeal setmeal);

    //套餐分页查询
    Page<SetmealVO> setmealPageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    //根据套餐id查询套餐
    @Select("select * from setmeal where id = #{id}")
    Setmeal getSetmealById(Long id);

    //根据套餐id查询套餐内未启售菜品的数量
    @Select("select count(*) from setmeal_dish sd left join dish d on d.id = sd.dish_id where sd.setmeal_id = #{id} and d.status = 0")
    Integer countDisabledDishBySetmealId(Long id);

    //根据套餐id集合批量删除套餐数据
    void deleteByIds(List<Long> ids);

    //修改套餐
    @AutoFill(value = OperationType.UPDATE)
    void updateSetmeal(Setmeal setmeal);
}
