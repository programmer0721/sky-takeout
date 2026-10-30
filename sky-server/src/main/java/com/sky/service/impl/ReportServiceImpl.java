package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkSpaceService workspaceService;

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

    //获取指定时间区间内销量前十商品
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        //销量前十商品列表
        List<GoodsSalesDTO> salesTop10List = new ArrayList<>();
        //获取时间区间内销量前十商品，放入列表
        LocalDateTime beginTime = LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);
        salesTop10List = orderMapper.getSalesTop10(beginTime,endTime);
        //从列表取出商品名称和销量，分别放入商品名称列表，销量列表，放入VO返回
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        for(GoodsSalesDTO good : salesTop10List){
            nameList.add(good.getName());
            numberList.add(good.getNumber());
        }
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

    /**导出近30天的运营数据报表
     * @param response
     **/
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        //查询概览运营数据，提供给Excel模板文件
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin,LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //基于提供好的模板文件创建一个新的Excel表格对象
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //获得Excel文件中的一个Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);
            //获得第4行
            XSSFRow row = sheet.getRow(3);
            //获取单元格
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                //准备明细数据
                businessData = workspaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }
            //通过输出流将文件下载到客户端浏览器中
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //关闭资源
            out.flush();
            out.close();
            excel.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
