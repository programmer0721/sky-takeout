## 地址簿管理（C 端）

地址簿是直接导入的现成代码，一套重复度很高的增删改查，所以这里不记手写过程。接口在
AddressBookController 里，前缀 /user/addressBook：GET /list 查当前用户全部地址，POST 新增，
GET /{id} 按 id 查，PUT 修改，PUT /default 设默认地址，DELETE 按 id 删除（id 走查询参数），
GET /default 查默认地址。除了设默认地址，其余都是把条件直接透给 mapper。

setDefault 在一个事务里做两步：先把当前用户所有地址的 is_default 改成 0，再把目标地址改成 1，
这样任何时刻最多只有一条默认地址，只做第二步就会出多条默认地址。list 和 save 会从
BaseContext 取当前登录用户，但导入的这套代码没做归属校验，getById、update、deleteById 都只按
主键操作，拿到别人的地址 id 就能改或删别人的地址，要严谨得在 service 里补一次 userId 比对。

## 订单（C 端）

订单这块目前只做了下单和支付两个接口，都在 OrderController 里，前缀 /user/order，
POST /submit 下单，PUT /payment 支付。下单的 orderSubmit 打了 @Transactional：先用
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
订单的 pay_method 一直是空的；用户端的订单查询、取消、再来一单和管理端的接单、拒单、派送、完成、
统计都还没写。
