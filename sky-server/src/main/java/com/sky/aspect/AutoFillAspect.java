package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

//自定义切面，实现公共字段自动填充逻辑
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
   //切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}
    //前置通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinpoint) throws NoSuchMethodException {
        log.info("正在自动填充公共字段");

        //获取被拦截的方法的数据库操作类型
        MethodSignature methodSignature = (MethodSignature) joinpoint.getSignature();//获取被拦截的方法的方法签名对象
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);//获取方法对象的注解
        OperationType operationType=autoFill.value();//获取注解的值

        //获取方法第一个参数，即实体对象
        Object[] args = joinpoint.getArgs();
        if (args == null || args.length == 0) {
            return;   // 没有实体参数，说明不是约定中的 insert/update，直接放行
        }
        Object entity=args[0];

        //准备赋值的属性
        LocalDateTime now = LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();

        //根据数据库操作类型，对公共字段进行赋值
        if(operationType == OperationType.UPDATE){
            try {
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else if(operationType == OperationType.INSERT){
            try {
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setCreateUser.invoke(entity, currentId);
                setCreateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
