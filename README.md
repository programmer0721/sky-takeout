新增公共字段自动填充功能：通过自定义注解 @AutoFill 标记 Mapper 层 insert/update 方法，
由 AOP 切面在执行前统一填充 create_time/update_time/create_user/update_user，
消除了 Service 层重复的公共字段赋值代码。