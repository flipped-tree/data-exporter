package com.cx.exporter.aspect;

import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Pointcut("@annotation(com.cx.exporter.annotation.EnableLog)")
    public void pointCut() {
    }

    @Around(value = "pointCut()")
    public Object log(ProceedingJoinPoint joinPoint) {
        // 开始打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();
        // 打印请求相关参数
        log.info("========================================== Start ==========================================");
        // 打印请求 url
        log.info("URL            : {}", request.getRequestURL().toString());
        // 打印 Http method
        log.info("HTTP Method    : {}", request.getMethod());
        // 打印调用 controller 的全路径以及执行方法
        log.info("Class Method   : {}.{}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
        // 打印请求的 IP
        log.info("IP             : {}", request.getRemoteAddr());
        // 打印请求入参
        log.info("Request Args   : {}", JSONUtil.toJsonPrettyStr(joinPoint.getArgs()));

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            // 打印出参
            log.info("Response Args  : {}", JSONUtil.toJsonPrettyStr(result));
            // 执行耗时
            log.info("Time-Consuming : {} ms", System.currentTimeMillis() - startTime);
        }
        return result;
    }

}
