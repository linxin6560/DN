package com.levylin.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Created by LinXin on 2018/3/17.
 */
@Aspect
public class BehaviorTraceAspect {

    @Pointcut("execution(@com.levylin.aop.BehaviorTrace * *(..))")
    public void methodAnnotated() {
        System.out.println("methodAnnotated..........");
    }

    @Around("methodAnnotated()")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        String funName = methodSignature.getMethod().getAnnotation(BehaviorTrace.class).value();
        Class[] paramsClzs = methodSignature.getParameterTypes();
        StringBuilder params = new StringBuilder();
        for (Class clz : paramsClzs) {
            params.append(clz.getName()).append(",");
        }
        String p = params.toString();
        if (params.length() != 0) {
            p = params.substring(0, params.length() - 1);
        }
        long time = System.currentTimeMillis();
        Object o = joinPoint.proceed();
        long diff = System.currentTimeMillis() - time;
        System.out.println(className + "." + methodName + "(" + p + ")----" + funName + "---花费时间:" + diff + "ms");
        return o;
    }
}
