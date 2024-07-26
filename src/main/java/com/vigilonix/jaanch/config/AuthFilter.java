package com.vigilonix.jaanch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
import com.vigilonix.jaanch.model.OAuthToken;
import com.vigilonix.jaanch.service.AuditService;
import com.vigilonix.jaanch.service.TokenService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class AuthFilter implements Filter {
    public static final String OAUTH_TOKEN = "/oauth/token";
    public static final String OAUTH_REFRESH_TOKEN = "/oauth/refresh_token";
    public static final String FAVICON_ICO = "/favicon.ico";
    public static final String V_3_API_DOCS = "/v3/api-docs";
    public static final String SWAGGER_UI = "/swagger-ui";
    public static final String V_1_ANON = "/v1/anon";
    private final static String AUTH_HEADER = "Authorization";
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final AuditService auditService;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            if (!(Arrays.asList(OAUTH_TOKEN, OAUTH_REFRESH_TOKEN, FAVICON_ICO)
                    .contains(httpServletRequest.getRequestURI())
                    || httpServletRequest.getRequestURI().startsWith(V_3_API_DOCS)
                    || httpServletRequest.getRequestURI().startsWith(SWAGGER_UI)
                    || httpServletRequest.getRequestURI().startsWith(V_1_ANON))) {
                String token = httpServletRequest.getHeader(AUTH_HEADER);
                if (StringUtils.isNotEmpty(token) && token.length() > 7) {
                    OAuthToken oauthToken = tokenService.validateToken(token.substring(7));
                    if (oauthToken == null) {
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, objectMapper.writeValueAsString(Collections.singletonList(ValidationErrorEnum.INVALID_TOKEN)));
                        return;
                    }
                    httpServletRequest.setAttribute(Constant.PRINCIPAL, oauthToken.getUser());
                    httpServletRequest.setAttribute(Constant.CLIENT_ID, oauthToken.getClientId());
                    auditService.audit(oauthToken.getUser(), httpServletRequest.getRequestURI(), null);
                } else {
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, objectMapper.writeValueAsString(Collections.singletonList(ValidationErrorEnum.INVALID_TOKEN)));
                    return;
                }
            }
            chain.doFilter(request, response);
            return;
        }
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, objectMapper.writeValueAsString(Collections.singletonList(ValidationErrorEnum.INVALID_TOKEN)));
    }

    @Override
    public boolean isLoggable(LogRecord logRecord) {
        return false;
    }
}
