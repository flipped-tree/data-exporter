package com.cx.exporter.aspect;

import com.cx.exporter.exception.ExecuteException;
import com.cx.exporter.utils.JsonUtil;
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
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            throw new ExecuteException(e);
        } finally {
            // 开始打印请求日志
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();
            log.info("""
                                ========================================== Start ==========================================
                                URL            : {},
                                HTTP Method    : {},
                                Class Method   : {}.{},
                                IP             : {},
                                Request Args   : {},
                                Response Args  : {},
                                Time-Consuming : {} ms
                                ========================================== End ==========================================
                            """,
                    request.getRequestURL().toString(),
                    request.getMethod(),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    request.getRemoteAddr(),
                    JsonUtil.obj2String(joinPoint.getArgs()),
                    JsonUtil.obj2String(result),
                    System.currentTimeMillis() - startTime);
        }
        return result;
    }

}
