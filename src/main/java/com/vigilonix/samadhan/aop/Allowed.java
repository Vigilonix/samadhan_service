package com.vigilonix.samadhan.aop;

import com.vigilonix.samadhan.enums.Role;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Allowed {
    Role[] roles() default Role.NORMAL;
}