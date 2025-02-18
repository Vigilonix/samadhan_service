package com.vigilonix.samadhan.helper;

import com.vigilonix.samadhan.config.Constant;
import com.vigilonix.samadhan.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Objects;

@Component
public class AuthHelper {
    public User getPrincipal() {
        return (User) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
                .getAttribute(Constant.PRINCIPAL, RequestAttributes.SCOPE_REQUEST);
    }

    public String getClientId() {
        return (String) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
                .getAttribute(Constant.CLIENT_ID, RequestAttributes.SCOPE_REQUEST);
    }
}
