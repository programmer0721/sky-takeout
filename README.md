## 配送范围校验

用户下单时，通过百度地图接口把店铺地址和收货地址解析成经纬度，再用驾车路线算出两点距离。超过 5000 米就抛出「超出配送范围」异常，拦截下单。

实现位于 `OrderServiceImpl.checkOutOfRange()`，在 `orderSubmit()` 里创建订单前调用。需要配置文件 `application-dev.yml` 里有 `sky.shop.address`（店铺地址）和 `sky.baidu.ak`（百度地图密钥）。异常由 `GlobalExceptionHandler` 统一处理，前端会收到提示「超出配送范围」。

注意：此功能依赖外网百度接口；`directionlite` 返回的 `routes` 可能为空、`distance` 强转可能报错，建议对空路由和 null 距离做判断，统一按「超出配送范围」抛异常。