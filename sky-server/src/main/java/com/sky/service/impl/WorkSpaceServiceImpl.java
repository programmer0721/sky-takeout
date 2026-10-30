package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;


@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    //工作台今日数据查询
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        /**
         * 营业额：当日已完成订单的总金额
         * 有效订单：当日已完成订单的数量
         * 订单完成率：有效订单数 / 总订单数
         * 平均客单价：营业额 / 有效订单数
         * 新增用户：当日新增用户的数量
         */
        Map map = new HashMap();
        map.put("beginTime",begin);
        map.put("endTime",end);
        map.put("status", Orders.COMPLETED);
        Double turnover = orderMapper.getTurnOverByMap(map);
        turnover = turnover == null ? 0.0 : turnover;

        Double validOrderCount = orderMapper.getOrdersMessageByTimeAndStatus(begin,end,Orders.COMPLETED);

        Double totalOrderCount = orderMapper.getOrdersMessageByTimeAndStatus(begin,end,null);

        Double orderCompletionRate = 0.0;

        Double unitPrice = 0.0;

        if(totalOrderCount != 0.0) {
            orderCompletionRate = validOrderCount / totalOrderCount;
        }

        if(validOrderCount != 0.0) {
            unitPrice = turnover / validOrderCount;
        }

        Double newUsers = userMapper.getUserMessageByTime(begin,end);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount.intValue())
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers.intValue())
                .build();
    }

    //查询订单管理数据
    public OrderOverViewVO getOrderOverView() {

        LocalDateTime beginTime = LocalDateTime.now().with(LocalTime.MIN);
        Double waitingOrders = orderMapper.getOrdersMessageByTimeAndStatus(beginTime,null,Orders.TO_BE_CONFIRMED);
        Double deliveredOrders = orderMapper.getOrdersMessageByTimeAndStatus(beginTime,null,Orders.CONFIRMED);
        Double completedOrders = orderMapper.getOrdersMessageByTimeAndStatus(beginTime,null,Orders.COMPLETED);
        Double cancelledOrders = orderMapper.getOrdersMessageByTimeAndStatus(beginTime,null,Orders.CANCELLED);
        Double allOrders = orderMapper.getOrdersMessageByTimeAndStatus(beginTime,null,null);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders.intValue())
                .deliveredOrders(deliveredOrders.intValue())
                .completedOrders(completedOrders.intValue())
                .cancelledOrders(cancelledOrders.intValue())
                .allOrders(allOrders.intValue())
                .build();
    }

    //查询菜品总览
    public DishOverViewVO getDishOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();

    }

    //查询套餐总览
    public SetmealOverViewVO getSetmealOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();

    }
}
