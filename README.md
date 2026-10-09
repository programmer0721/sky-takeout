## 订单（C 端）

订单都在 OrderController 里，前缀 /user/order，一共六个：POST /submit 下单，PUT /payment 支付，
GET /historyOrders 历史订单分页，GET /orderDetail/{id} 订单详情，PUT /cancel/{id} 取消订单，
POST /repetition/{id} 再来一单。下单的 orderSubmit 打了 @Transactional：先用
addressBookId 查地址簿，查不到抛 AddressBookBusinessException，再按当前用户查购物车，
空的话抛 ShoppingCartBusinessException；然后组装订单落 orders 表，订单号用
System.currentTimeMillis()，状态写待付款 1、支付状态写未支付 0，收货人、电话、详细地址这三个
字段用地址簿里的值覆盖前端传的；接着把购物车每一条按字段名拷进 OrderDetail，补上 orderId 后
批量插入 order_detail，最后清空该用户的购物车，返回订单 id、订单号、金额、下单时间。
金额直接用前端传的 amount，服务端没有重算，正式环境应该在这里按购物车自己算一遍。

支付没有接真实的微信支付。WeChatPayUtil 是走 /v3/pay/transactions/jsapi 下单、再签名给
小程序唤起收银台的，需要商户号、商户私钥和平台证书，个人开发者申请不到商户号，所以这段调用
整段注释掉了，只留了一个 log 再调 paySuccess，直接按支付成功处理。注释里保留了原来的写法
（按 userId 查 User 取 openid、调 weChatPayUtil.pay 生成预支付交易单、判断返回的 code 是不是
ORDERPAID），以后拿到商户号把注释放开、把证书和 sky.wechat 配置补齐就能用；UserMapper 新增的
getUserById 和 OrderServiceImpl 里注入的 WeChatPayUtil 就是给它预留的，目前只有注释里的代码在引用。

paySuccess 按订单号查出订单，把 status 改成待接单 2、payStatus 改成已支付 1、checkoutTime 写当前
时间，靠 OrderMapper.update 的动态 set 只更新传进去的非 null 字段。几个要注意的点：payment
没有校验订单是否存在和当前状态，订单号传错会直接空指针；OrdersPaymentDTO 里的 payMethod 没落库，
订单的 pay_method 一直是空的。

历史订单分页的入参就是 OrdersPageQueryDTO 本身，page、pageSize、status 这些由前端通过查询参数
绑进来，userId 在 service 里覆盖成当前登录用户，所以只能查自己的单（管理端以后复用同一个查询时
不能再这么覆盖，否则管理员只会看到自己名下那批）。流程是 PageHelper.startPage 把分页参数塞进
ThreadLocal，紧跟着的那次 mapper 调用被拦截器改写加上 limit，并额外发一条 count 查总数，所以
startPage 和这次查询中间不能插别的查询。主表那条 SQL 的 resultType 直接写 com.sky.vo.OrderVO，
where 按 number、phone、userId、status、时间区间动态拼，order by order_time desc。拿到这一页后
遍历补 orderDetailList，最后返回 PageResult(page.getTotal(), page)。Page 继承 ArrayList，它本身
就是这一页的数据，total 那些是附加信息，直接当 records 传出去 Jackson 会序列化成纯数组，不会多带
字段。目前是一页 N 单查 N 次明细的 N+1 写法，要优化可以把 orderId 收集起来一次 in 查完再按
orderId 分组塞回去。

订单详情按 id 查 orders 拷进 OrderVO 再补明细；查不到要抛 OrderBusinessException(ORDER_NOT_FOUND)，
不判空的话 BeanUtils.copyProperties(null, vo) 会抛 IllegalArgumentException，变成 500。

取消订单只允许待付款和待接单（status 小于等于 2），否则抛 ORDER_STATUS_ERROR。更新走的是动态 set，
所以 new 一个空的 Orders，只塞 id 和这次要改的字段：status 置为已取消、cancelReason 填“用户取消”、
cancelTime 取当前时间；如果取消时是待接单，说明钱已经付过，还要把 payStatus 置成退款状态，
微信退款那段和支付一样注释掉了。业务失败统一抛 OrderBusinessException，由 GlobalExceptionHandler
转成 Result.error，所以 controller 上那个 throws Exception 是多余的，可以删。

再来一单是把该订单的 order_detail 整批塞回购物车：逐条拷成 ShoppingCart（忽略 id，因为购物车主键
要自增），补上 userId 和 createTime，再用 ShoppingCartMapper.insertBatch 一次批量插入，前端拿到
成功就跳购物车页，真正下单还是走 submit。已知的简化：直接插新行，不跟购物车里已有的菜合并，同一道菜
会出现两条；菜品名、图片、价格照搬历史快照，改过价或已停售的菜也会原样加回来；接口没做归属校验，
而且订单不存在时明细是空 list，foreach 拼出来的 SQL 是 values 后面没内容，会直接报语法错误，
补一句订单存在性判断能一起解决。

管理端的接单、拒单、派送、完成、统计都还没写。
