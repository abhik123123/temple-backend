package com.temple.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs controller timings. Deliberately does NOT log method arguments or
 * return values: those contain passwords, tokens and devotees' personal data.
 * Exceptions are logged once, centrally, by GlobalExceptionHandler.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("com.temple.api");
    private static final long SLOW_MS = 1000;

    @Around("execution(public * com.temple.controller..*(..))")
    public Object timeController(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        String endpoint = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        boolean ok = false;
        try {
            Object result = joinPoint.proceed();
            ok = true;
            return result;
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            if (ms >= SLOW_MS) {
                log.warn("SLOW_REQUEST | endpoint={} | duration={}ms | ok={}", endpoint, ms, ok);
            } else if (log.isDebugEnabled()) {
                log.debug("REQUEST | endpoint={} | duration={}ms | ok={}", endpoint, ms, ok);
            }
        }
    }
}
