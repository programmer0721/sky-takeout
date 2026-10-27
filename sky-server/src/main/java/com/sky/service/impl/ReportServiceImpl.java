package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    //获取指定时间区间内的每一天的营业额
    public TurnoverReportVO getTurnOver(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnOverList = new ArrayList<>();
        LocalDate time = begin;
        while (!time.isAfter(end)) {
            dateList.add(time);
            time = time.plusDays(1);
        }
        for(LocalDate day : dateList){
            Map map = new HashMap();
            LocalDateTime beginTime = LocalDateTime.of(day, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(day, LocalTime.MAX);
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);
            map.put("status",Orders.COMPLETED);
            Double turnOver = orderMapper.getTurnOverByMap(map);
            turnOver = turnOver == null ? 0.0 : turnOver;
            turnOverList.add(turnOver);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnOverList,","))
                .build();
    }

    //获取指定时间区间内的新增用户以及截至目前总用户数量
    public UserReportVO getUser(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> totalUserList = new ArrayList<>();
        List<Double> newUserList = new ArrayList<>();
        LocalDate time = begin;
        while (!time.isAfter(end)) {
            dateList.add(time);
            time = time.plusDays(1);
        }
        for (LocalDate day : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(day,LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(day,LocalTime.MAX);
            Double newUser = userMapper.getUserMessageByTime(beginTime,endTime);
            Double totalUser = userMapper.getUserMessageByTime(null,endTime);
            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }
        return UserReportVO.builder().dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    //获取指定时间区间内的每日订单数，每日有效订单数，区间内订单总数，区间内有效订单数，以及订单完成率（有效/总数）
    public OrderReportVO getOrders(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        //每日订单数
        List<Double> orderCountList = new ArrayList<>();
        //每日有效订单数
        List<Double> validOrderCountList = new ArrayList<>();
        LocalDate time = begin;
        while (!time.isAfter(end)) {
            dateList.add(time);
            time = time.plusDays(1);
        }
        for (LocalDate day : dateList){
            LocalDateTime beginTime = LocalDateTime.of(day,LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(day,LocalTime.MAX);
            Double orderCount = orderMapper.getOrdersMessageByTimeAndStatus(beginTime,endTime,null);
            Double validOrderCount = orderMapper.getOrdersMessageByTimeAndStatus(beginTime,endTime,Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        //区间内订单总数
        double totalOrderCount = 0;
        for(Double num : orderCountList){
           totalOrderCount += num;
        }
        //区间内有效订单数
        double validOrderCount = 0.0;
        for(Double num : validOrderCountList){
            validOrderCount += num;
        }
        //订单完成率（有效/总数）
        double orderCompletionRate = 0.0;
        if(totalOrderCount != 0.0){
             orderCompletionRate = validOrderCount/totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount((int)totalOrderCount)
                .validOrderCount((int)validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
}
