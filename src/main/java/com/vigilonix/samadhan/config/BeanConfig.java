package com.vigilonix.samadhan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.vigilonix.samadhan.enums.NotificationMethod;
import com.vigilonix.samadhan.enums.OdApplicationStatus;
import com.vigilonix.samadhan.helper.FirebaseCloudMessageNotificationWorker;
import com.vigilonix.samadhan.helper.FirestoreWriter;
import com.vigilonix.samadhan.helper.INotificationWorker;
import com.vigilonix.samadhan.helper.CherrioWhatsappNotificationWorker;
import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.pojo.*;
import com.vigilonix.samadhan.request.AuthRequest;
import com.vigilonix.samadhan.request.UserRequest;
import com.vigilonix.samadhan.transformer.*;
import com.vigilonix.samadhan.validator.*;
import jakarta.servlet.MultipartConfigElement;
import org.apache.commons.collections4.Transformer;
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
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@EnableTransactionManagement
@Configuration
@ComponentScan({"com.vigilonix.samadhan"})
@EnableJpaRepositories(basePackages = "com.vigilonix.samadhan.repository")
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
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // Use allowedOriginPattern instead of addAllowedOrigin
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
    @Qualifier("create")
    public ValidationService<ODApplicationValidationPayload> odCreaateValidationService(OdApplicationCreationValidator odApplicationCreationValidator) {
        return () -> Collections.singletonList(odApplicationCreationValidator);
    }

    @Bean
    @Qualifier("update")
    public ValidationService<ODApplicationValidationPayload> odUpdateValidationService(OdApplicationUpdateValidator odApplicationUpdateValidator) {
        return () -> Collections.singletonList(odApplicationUpdateValidator);
    }

    @Bean
    @Qualifier("ROOT_NODE")
    public GeoHierarchyNode parseGeoHierarchyNodes(@Autowired ResourceLoader resourceLoader, @Autowired ObjectMapper objectMapper) {
        Resource resource = resourceLoader.getResource(Constant.GEOFENCE_HIERARCHY);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, GeoHierarchyNode.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse geofence_hierarchy.json file", e);
        }
    }

    @Bean
    public Map<OdApplicationStatus, List<Transformer<OdApplication, NotificationPayload>>> getNotificationPayloadTransformer(ApplicantApplicationCreationWhatasappDirectTransformer applicantApplicationCreationWhatasappDirectTransformer,
                                                                                                                             ApplicantApplicationCreationWhatasappTextTemplateTransformer applicantApplicationCreationWhatasappTextTemplateTransformer,
                                                                                                                             ApplicantApplicationClosedWhatasappDocumentTemplateTransformer applicantApplicationClosedWhatasappDocumentTemplateTransformer,
                                                                                                                             ApplicationPendingGeoFenceOwnerFirebaseCloudMessageTransformer  applicationPendingGeoFenceOwnerFirebaseCloudMessageTransformer,
                                                                                                                             ApplicantApplicationCreationWhatasappDocumentReplyTemplateTransformer applicantApplicationCreationWhatasappDocumentReplyTemplateTransformer,
                                                                                                                             ApplicationFirestoreStateChangeSSETransformer applicationFirestoreStateChangeSSETransformer) {
        Map<OdApplicationStatus, List<Transformer<OdApplication, NotificationPayload>>> templateTransformerMap = new HashMap<>();
        templateTransformerMap.put(OdApplicationStatus.OPEN, Arrays.asList(applicationPendingGeoFenceOwnerFirebaseCloudMessageTransformer, applicantApplicationCreationWhatasappDocumentReplyTemplateTransformer, applicationFirestoreStateChangeSSETransformer));
        templateTransformerMap.put(OdApplicationStatus.ENQUIRY,  Arrays.asList(applicationFirestoreStateChangeSSETransformer));
        templateTransformerMap.put(OdApplicationStatus.REVIEW,  Arrays.asList(applicationFirestoreStateChangeSSETransformer));
        templateTransformerMap.put(OdApplicationStatus.CLOSED,  Arrays.asList(applicantApplicationClosedWhatasappDocumentTemplateTransformer, applicationFirestoreStateChangeSSETransformer));
        return templateTransformerMap;
    }

    @Bean
    public Map<NotificationMethod, NavigableSet<INotificationWorker>> getNotificationWorkerMap(
            CherrioWhatsappNotificationWorker cherrioWhatsappNotificationWorker,
            FirebaseCloudMessageNotificationWorker firebaseCloudMessageNotificationWorker,
            FirestoreWriter firestoreWriter) {
        Map<NotificationMethod, NavigableSet<INotificationWorker>> notificationWorkerMap = new HashMap<>();
        NavigableSet<INotificationWorker> whatsappWorkers = new TreeSet<>(Comparator.comparingInt(INotificationWorker::getPriority).reversed());
        whatsappWorkers.add(cherrioWhatsappNotificationWorker);

        NavigableSet<INotificationWorker>  NotificationCloudMessageWorkers= new TreeSet<>(Comparator.comparingInt(INotificationWorker::getPriority).reversed());
        NotificationCloudMessageWorkers.add(firebaseCloudMessageNotificationWorker);

        NavigableSet<INotificationWorker> sseWorkers= new TreeSet<>(Comparator.comparingInt(INotificationWorker::getPriority).reversed());
        sseWorkers.add(firestoreWriter);

        // Put the TreeSet into the map
        notificationWorkerMap.put(NotificationMethod.WHATSAPP_TEMPLATE, whatsappWorkers);
        notificationWorkerMap.put(NotificationMethod.NOTIFICATION_CLOUD_MESSAGE, NotificationCloudMessageWorkers);
        notificationWorkerMap.put(NotificationMethod.SSE_EVENT, sseWorkers);

        return notificationWorkerMap;
    }

    @Bean
    public FirebaseApp initFirebase(FirebaseConf firebaseConf) throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream(firebaseConf.getPrivateFilePath());

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(firebaseConf.getDatabaseUrl())
                .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public Firestore getFirestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore();
    }
}
