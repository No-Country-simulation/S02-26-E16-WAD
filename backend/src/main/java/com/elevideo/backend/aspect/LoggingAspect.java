package com.elevideo.backend.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // Intercepta todos los controladores REST
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String method = "N/A", uri = "N/A";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            method = request.getMethod();
            uri = request.getRequestURI();
        }

        String controller = joinPoint.getTarget().getClass().getSimpleName();
        String action = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        String args = Arrays.toString(joinPoint.getArgs());

        long start = System.currentTimeMillis();
        log.info("🌐 REQUEST [{} {}] -> {}.{}(..) | Args: {}", method, uri, controller, action, args);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            if (result instanceof ResponseEntity<?> responseEntity) {
                int status = responseEntity.getStatusCode().value();
                log.info("✅ RESPONSE [{} {}] -> Status: {} | Duration: {} ms", method, uri, status, duration);
            } else {
                log.info("✅ RESPONSE [{} {}] -> Success | Duration: {} ms", method, uri, duration);
            }

            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("❌ ERROR [{} {}] -> {}.{}(..) | Duration: {} ms | Message: {}",
                    method, uri, controller, action, duration, ex.getMessage(), ex);
            throw ex;
        }
    }

    // Intercepta métodos anotados con @LogExecution
    @Around("@annotation(logExecution)")
    public Object logExecution(ProceedingJoinPoint joinPoint, LogExecution logExecution) throws Throwable {
        String traceId = MDC.get("traceId");
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());
        String message = logExecution.value();

        long start = System.currentTimeMillis();
        log.info("▶️ START {}.{}(..) - {} | Args: {}", className, methodName, message, args);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            if (result != null)
                log.info("✅ SUCCESS {}.{}(..) - {} | Duration: {} ms | Result: {}",
                        className, methodName, message, duration, result);
            else
                log.info("✅ SUCCESS {}.{}(..) - {} | Duration: {} ms]",
                        className, methodName, message, duration);

            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("❌ ERROR {}.{}(..) - {} | Duration: {} ms | Message: {}]",
                    className, methodName, message, duration, ex.getMessage(), ex);
            throw ex;
        }
    }
}