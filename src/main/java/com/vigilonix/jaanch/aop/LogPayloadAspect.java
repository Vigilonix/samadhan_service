package com.vigilonix.jaanch.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LogPayloadAspect {

    public static final String TIMED_ASPECT_TIME_TAKEN_FOR_IS_MS = "Timed aspect: time taken for {} is {} ms";

    @Around("@annotation(com.dt.beyond.aop.LogPayload)")
    public Object timed(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        log.info("logging payload for {} with args {}", methodSignature.getMethod().toString(), args);
        return joinPoint.proceed();
    }
}
