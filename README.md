用SpringCache实现了缓存套餐和缓存菜品：
- 启动类加 `@EnableCaching` 开启注解缓存；缓存实现是 Redis（`spring-boot-starter-cache` + `data-redis`）。
- 读缓存（C 端两个列表查询）：`dishCache`、`setmealCache`，key 都取 `#categoryId`，
  Redis 里的键形如 `dishCache::18`。
- 清缓存（管理端写操作）：
  - 新增菜品/套餐 → `@CacheEvict(key = "#dishDTO.categoryId")`，只清受影响的那个分类；
  - 批量删除、修改、起售停售 → `@CacheEvict(allEntries = true)`，因为这些入口拿不到分类
    （删除只有一批 ids、停售只有 id、修改还可能把菜品换到别的分类）。
- `@Cacheable` 的 key 与 `@CacheEvict` 的 key 必须一一对应，否则清的是不存在的键，
  数据会一直脏而且**不报错**。
- `@CacheEvict` 默认在方法成功返回后执行；事务注解在 service 上、controller 在事务之外，
  因此清理发生在事务提交之后。
- 刻意不加缓存的地方：管理端分页与按 id 查询（运营需要实时数据）、
  C 端套餐内菜品列表（同时受套餐和菜品两条写线影响，失效点太多）。
- 待改进：当前缓存没有 TTL（永不过期），建议配 `spring.cache.redis.time-to-live` 做兜底。
