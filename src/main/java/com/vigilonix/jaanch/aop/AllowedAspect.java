package com.vigilonix.jaanch.aop;

import com.dt.beyond.config.AuthHelper;
import com.dt.beyond.enums.ValidationErrorEnum;
import com.dt.beyond.exception.ValidationRuntimeException;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AllowedAspect {
    public static final String ANNOTATION_COM_DT_BEYOND_AOP_ALLOWED = "@annotation(com.dt.beyond.aop.Allowed)";
    private final AuthHelper authHelper;

    @Around(ANNOTATION_COM_DT_BEYOND_AOP_ALLOWED)
    public Object timed(final ProceedingJoinPoint joinPoint) throws Throwable {
        final String methodName = joinPoint.getSignature().getName();
        final MethodSignature methodSignature = (MethodSignature) joinPoint
                .getSignature();
        Method method = methodSignature.getMethod();
        Allowed annotation = method.getAnnotation(Allowed.class);

        if (Sets.newHashSet(annotation.roles()).contains(authHelper.getPrincipal().getRole())) {
            return joinPoint.proceed();
        } else {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.UNAUTHORIZED_REQUEST));
        }
    }
}
