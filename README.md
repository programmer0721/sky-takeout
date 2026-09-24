
## 购物车相关接口（C 端）

购物车接口在 ShoppingCartController 里，统一前缀 /user/shoppingCart，一共四个：新增商品是
POST /add，查看购物车是 GET /list，商品数量减一是 POST /sub，清空购物车是 DELETE /clean。
请求体都是 ShoppingCartDTO，只有 dishId、setmealId、dishFlavor 三个字段，菜品和套餐二选一，
dishFlavor 只有菜品才带。

用户身份不从请求里取，四个接口都用 BaseContext.getCurrentId()：add、list、sub 在 service 里
把 userId 覆盖成当前登录用户，clean 直接把 userId 传进 where，所以只能读到自己那一条。
整个 /user/** 都注册了 JwtTokenUserInterceptor，没带 C 端令牌会返回 401。

购物车里存的是快照。name、image、amount 在加入购物车时从 dish 或 setmeal 表里取一次写死，
之后菜品改价不影响车里已有的数据，结算时按库里最新价重新算是订单模块的事；价格取库里的
dish.price 或 setmeal.price，不由前端传。

add 是先查、有则加、无则插：按 userId 加 dishId/setmealId/dishFlavor 查一次，查到就把 number 加一
（updateNumberById 只更新 number 一个字段），查不到才查出名称、图片、单价，写 number 为 1 和 createTime
后插入。查询条件在 ShoppingCartMapper.xml 里用 if 动态拼，只有非 null 的字段才进 where，
别名的生效依赖 type-aliases-package 配了 com.sky.entity。

几个边界要注意。dishFlavor 传 null 时查询条件会退化成只按 dishId 匹配，可能把“微辣”那条当成
同一条商品去累加数量，前端对同一菜品要保持一致；dishId 和 setmealId 同时为 null 时会走到
getSetmealById(null) 再取名称，直接空指针；add 和 sub 都是先查后写，没有并发控制，
同一用户快速重复点击有丢更新的风险；shopping_cart 的 user_id 建议加索引，
clean 是删除当前用户的全部数据，写 SQL 时别漏 where。
