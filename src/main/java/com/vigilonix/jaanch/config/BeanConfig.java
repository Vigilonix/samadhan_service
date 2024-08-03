package com.vigilonix.jaanch.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigilonix.jaanch.pojo.FieldGeoNode;
import com.vigilonix.jaanch.request.AuthRequest;
import com.vigilonix.jaanch.request.UserRequest;
import com.vigilonix.jaanch.validator.ClientValidator;
import com.vigilonix.jaanch.validator.UserRequestValidator;
import com.vigilonix.jaanch.validator.ValidationService;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@EnableTransactionManagement
@Configuration
@ComponentScan({"com.vigilonix.jaanch"})
@EnableJpaRepositories(basePackages = "com.vigilonix.jaanch.repository")
public class BeanConfig {


    @Bean
    @Primary
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setFileSizeThreshold(DataSize.ofMegabytes(10));
        return factory.createMultipartConfig();
    }

    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor() {
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardOldestPolicy();
        int poolSize = 1;
        int queueSize = 128;
        return new ThreadPoolExecutor(poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize),
                handler);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public ValidationService<UserRequest> userRequestValidationService(UserRequestValidator userRequestValidator) {
        return () -> Collections.singletonList(userRequestValidator);
    }
    @Bean
    public ValidationService<AuthRequest> clientValidatorService(ClientValidator clientValidator) {
        return () -> Collections.singletonList(clientValidator);
    }

    @Bean
    @Qualifier("ROOT_NODE")
    public FieldGeoNode parseFieldGeoNodes(@Autowired ResourceLoader resourceLoader,@Autowired ObjectMapper objectMapper) {
        Resource resource = resourceLoader.getResource(Constant.GEOFENCE_HIERARCHY);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, FieldGeoNode.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse geofence_hierarchy.json file", e);
        }
    }
}
