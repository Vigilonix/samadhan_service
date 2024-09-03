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
public class TimedAspect {

    public static final String TIMED_ASPECT_TIME_TAKEN_FOR_IS_MS = "Timed aspect: time taken for {} is {} ms";

    @Around("@annotation(com.vigilonix.jaanch.aop.Timed)")
    public Object timed(final ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            log.info(TIMED_ASPECT_TIME_TAKEN_FOR_IS_MS, methodSignature.getMethod().toString(), endTime - startTime);
        }
    }
}
