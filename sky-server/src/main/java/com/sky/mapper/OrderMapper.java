package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    //向订单表插入订单数据
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    //订单分页查询
    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    //根据订单id查询订单
    @Select("select * from orders where id = #{orderId}")
    Orders getOrderByOrderId(Long id);

    //查询某状态订单数量
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    //根据状态和下单时间查询订单
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getOrderByStatusAndTime(Integer status, LocalDateTime time);

    //根据起止时间和状态查询营业额
    Double getTurnOverByMap(Map map);

    //根据起止时间和状态查询订单数量
    Double getOrdersMessageByTimeAndStatus(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    //获取指定区间销量前十商品名称以及其销量
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
