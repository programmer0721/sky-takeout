## 

定时处理写在 `com.sky.task.OrderTask`，用 `@Scheduled` 注解实现，启动类上已加 `@EnableScheduling`。`processTimeoutOrder()` 每分钟执行一次，查出「待付款」且下单时间超过 15 分钟的订单，改为「已取消」，并写入取消原因「支付超时，自动取消」和取消时间。`processDeliveryOrder()` 每天凌晨 1 点执行一次，查出「派送中」且下单时间超过 60 分钟的订单，改为「已完成」。两个任务都通过 `OrderMapper.getOrderByStatusAndTime()` 查询，条件是订单状态加下单时间。

来单提醒和客户催单都基于 WebSocket 实现。`com.sky.websocket.WebSocketServer` 用 `@ServerEndpoint("/ws/{sid}")` 暴露端点，把每个连接的会话按 sid 存进 sessionMap；`WebSocketConfiguration` 里注册 `ServerEndpointExporter`，把端点交给内嵌 Tomcat 容器。订单支付成功后，`OrderServiceImpl.paySuccess()` 把 `{type:1, orderId, content}` 群发给所有在线客户端，管理端收到后按 type=1 播放提醒音并弹出「待接单」。客户催单由用户端接口 `GET /user/order/reminder/{id}` 触发，`OrderServiceImpl.reminder()` 先校验订单存在，再群发 `{type:2, orderId, content}`，管理端按 type=2 弹出「催单」。

注意：管理端前端连接的是 `ws://localhost/ws/<随机sid>`，经 nginx 的 `location /ws/` 反向代理到后端 8080，所以 nginx 里必须保留 `Upgrade`、`Connection` 头的透传和较长的 `proxy_read_timeout`。当前的消息是群发，既不区分客户端身份也没有鉴权，任何连上的客户端都会收到全部订单消息。另外「派送中」的判断依据是下单时间、且每天只跑一次，实际效果是凌晨清理前一天的遗留订单，并不是配送超过 60 分钟的实时判定。
