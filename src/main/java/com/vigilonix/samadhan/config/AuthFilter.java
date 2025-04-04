package com.vigilonix.samadhan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigilonix.samadhan.enums.ValidationErrorEnum;
import com.vigilonix.samadhan.model.OAuthToken;
import com.vigilonix.samadhan.service.AuditService;
import com.vigilonix.samadhan.service.TokenService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class AuthFilter implements Filter {
    public static final String OAUTH_TOKEN = "/oauth/token";
    public static final String OAUTH_REFRESH_TOKEN = "/oauth/refresh_token";
    public static final String FAVICON_ICO = "/favicon.ico";
    public static final String V_3_API_DOCS = "/api-docs";
    public static final String SWAGGER_UI = "/swagger";
    public static final String V_1_ANON = "/v1/anon";
    private final static String AUTH_HEADER = "Authorization";
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final AuditService auditService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            if (!(Arrays.asList(OAUTH_TOKEN, OAUTH_REFRESH_TOKEN, FAVICON_ICO)
                    .contains(httpServletRequest.getRequestURI())
                    || httpServletRequest.getRequestURI().startsWith(V_3_API_DOCS)
                    || httpServletRequest.getRequestURI().startsWith(SWAGGER_UI)
                    || httpServletRequest.getRequestURI().startsWith(V_1_ANON))) {
                String token = httpServletRequest.getHeader(AUTH_HEADER);;
                if (StringUtils.isNotEmpty(token) && token.length() > 7) {
                    OAuthToken oauthToken = tokenService.validateToken(token.substring(7));
                    if (oauthToken == null) {
                        log.error("invalid request for token {}", token);
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, objectMapper.writeValueAsString(Collections.singletonList(ValidationErrorEnum.INVALID_TOKEN)));
                        return;
                    }
                    httpServletRequest.setAttribute(Constant.PRINCIPAL, oauthToken.getUser());
                    httpServletRequest.setAttribute(Constant.CLIENT_ID, oauthToken.getClientId());
                    auditService.audit(oauthToken.getUser(), httpServletRequest.getRequestURI(), httpServletRequest.getMethod(),  null);
                } else {
                    log.error("unprotected invalid request for {}", httpServletRequest.getRequestURI());
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, objectMapper.writeValueAsString(Collections.singletonList(ValidationErrorEnum.INVALID_TOKEN)));
                    return;
                }
            }
            chain.doFilter(request, response);
            return;
        }
        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, objectMapper.writeValueAsString(Collections.singletonList(ValidationErrorEnum.INVALID_TOKEN)));
    }
}
