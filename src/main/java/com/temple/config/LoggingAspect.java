package com.temple.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * Logging Aspect for CloudWatch Integration
 * Automatically logs method entry, exit, duration, and exceptions
 * with structured data for easy CloudWatch Insights queries
 */
@Aspect
@Component
public class LoggingAspect {

    /**
     * Log around all controller methods
     */
    @Around("execution(* com.temple.controller.*.*(..))")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        // Add to MDC for correlation
        MDC.put("requestId", requestId);
        MDC.put("class", className);
        MDC.put("method", methodName);

        long startTime = System.currentTimeMillis();

        log.info("CONTROLLER_START | method={} | class={} | args={}",
                methodName, className, Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("CONTROLLER_SUCCESS | method={} | duration={}ms | result={}",
                    methodName, duration, result != null ? result.getClass().getSimpleName() : "null");

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("CONTROLLER_ERROR | method={} | duration={}ms | error={} | message={}",
                    methodName, duration, e.getClass().getSimpleName(), e.getMessage(), e);

            throw e;

        } finally {
            MDC.clear();
        }
    }

    /**
     * Log around all service methods
     */
    @Around("execution(* com.temple.service.*.*(..))")
    public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();

        log.debug("SERVICE_START | method={} | class={} | args={}",
                methodName, className, Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("SERVICE_SUCCESS | method={} | duration={}ms", methodName, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("SERVICE_ERROR | method={} | duration={}ms | error={} | message={}",
                    methodName, duration, e.getClass().getSimpleName(), e.getMessage(), e);

            throw e;
        }
    }

    /**
     * Log around repository methods
     */
    @Around("execution(* com.temple.repository.*.*(..))")
    public Object logAroundRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();

        log.trace("REPOSITORY_START | method={} | class={}", methodName, className);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.trace("REPOSITORY_SUCCESS | method={} | duration={}ms", methodName, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("REPOSITORY_ERROR | method={} | duration={}ms | error={} | message={}",
                    methodName, duration, e.getClass().getSimpleName(), e.getMessage(), e);

            throw e;
        }
    }

    /**
     * Log all exceptions thrown in the application
     */
    @AfterThrowing(pointcut = "execution(* com.temple..*(..))", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        log.error("EXCEPTION_THROWN | class={} | method={} | exceptionType={} | message={}",
                className, methodName, exception.getClass().getSimpleName(), exception.getMessage(), exception);
    }
}

