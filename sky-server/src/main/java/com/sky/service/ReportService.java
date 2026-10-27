package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    //获取指定时间区间内的每一天的营业额
    TurnoverReportVO getTurnOver(LocalDate begin, LocalDate end);

    //获取指定时间区间内的新增用户以及截至目前总用户数量
    UserReportVO getUser(LocalDate begin, LocalDate end);

    //获取指定时间区间内的每日订单数，每日有效订单数，区间内订单总数，区间内有效订单数，以及订单完成率（有效/总数）
    OrderReportVO getOrders(LocalDate begin, LocalDate end);
}
