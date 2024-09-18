package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.aop.LogPayload;
import com.vigilonix.jaanch.aop.Timed;
import com.vigilonix.jaanch.enums.Post;
import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.*;
import com.vigilonix.jaanch.repository.OdApplicationRepository;
import com.vigilonix.jaanch.repository.UserRepository;
import com.vigilonix.jaanch.transformer.OdApplicationTransformer;
import com.vigilonix.jaanch.validator.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Transactional
public class OdApplicationService {
    private final OdApplicationRepository odApplicationRepository;
    private final OdApplicationTransformer odApplicationTransformer;
    private final UserRepository userRepository;
    private final GeoHierarchyService geoHierarchyService;
    private final ValidationService<ODApplicationValidationPayload> odUpdateValidationService;
    private final ValidationService<ODApplicationValidationPayload> odCreateValidationService;
    private final NotificationService notificationService;

    @Autowired
    public OdApplicationService(
            OdApplicationRepository odApplicationRepository,
            OdApplicationTransformer odApplicationTransformer,
            UserRepository userRepository,
            GeoHierarchyService geoHierarchyService,
            @Qualifier("update") ValidationService<ODApplicationValidationPayload> odUpdateValidationService,
            @Qualifier("create") ValidationService<ODApplicationValidationPayload> odCreateValidationService, NotificationService notificationService) {
        this.odApplicationRepository = odApplicationRepository;
        this.odApplicationTransformer = odApplicationTransformer;
        this.userRepository = userRepository;
        this.geoHierarchyService = geoHierarchyService;
        this.odUpdateValidationService = odUpdateValidationService;
        this.odCreateValidationService = odCreateValidationService;
        this.notificationService = notificationService;
    }

    @Timed
    @LogPayload
    public OdApplicationPayload create(OdApplicationPayload odApplicationPayload, User principal) {
        odCreateValidationService.validate(ODApplicationValidationPayload.builder().odApplicationPayload(odApplicationPayload).principalUser(principal).build());

        Long epoch = System.currentTimeMillis();
        GeoHierarchyNode geoHierarchyNode = Objects.isNull(odApplicationPayload.getGeoHierarchyNodeUuid())
                ? geoHierarchyService.getHighestPostNode(principal.getPostGeoHierarchyNodeUuidMap())
                : geoHierarchyService.getNodeById(odApplicationPayload.getGeoHierarchyNodeUuid());
        Optional<Integer> maxBucketNo = odApplicationRepository.findMaxReceiptBucketNumberForCurrentMonth(geoHierarchyNode.getUuid());
        int bucketNo = maxBucketNo.map(integer -> integer + 1).orElse(1);

        OdApplication odApplication = OdApplication.builder()
                .geoHierarchyNodeUuid(geoHierarchyNode.getUuid())
                .uuid(UUID.randomUUID())
                .od(principal)
                .applicantName(odApplicationPayload.getApplicantName())
                .applicationFilePath(odApplicationPayload.getApplicationFilePath())
                .applicantPhoneNumber(odApplicationPayload.getApplicantPhoneNumber())
                .receiptNo(generateReceiptNumber(geoHierarchyNode, bucketNo))
                .receiptBucketNumber(bucketNo)
                .geoHierarchyNodeUuid(geoHierarchyNode.getUuid())
                .createdAt(epoch)
                .modifiedAt(epoch)
                .status(OdApplicationStatus.OPEN)
                .build();
        odApplicationRepository.save(odApplication);
        notificationService.sendNotification(odApplication);
        return odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build());
    }

    private String generateReceiptNumber(GeoHierarchyNode geoHierarchyNode, int bucketNo) {
        String jurisdictionName = geoHierarchyNode.getName()
                .replace(" ", "_");
        // Get the current date
        LocalDate currentDate = LocalDate.now();
        // Define the date formatter with the desired pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy");
        // Format the current date
        String formattedDate = currentDate.format(formatter);
        return String.format("%s_%s_%s", jurisdictionName, formattedDate, bucketNo);
    }

    @LogPayload
    @Timed
    public OdApplicationPayload update(UUID uuid, OdApplicationPayload odApplicationPayload, User principal) {
        OdApplication odApplication = odApplicationRepository.findByUuid(uuid);
        odUpdateValidationService.validate(ODApplicationValidationPayload.builder()
                .odApplicationPayload(odApplicationPayload)
                .odApplication(odApplication)
                .enquiryUser(userRepository.findByUuid(odApplicationPayload.getEnquiryOfficerUuid()))
                .principalUser(principal).build());
        if (!Objects.isNull(odApplicationPayload.getEnquiryOfficerUuid())) {
            User enquiryOfficer = userRepository.findByUuid(odApplicationPayload.getEnquiryOfficerUuid());
            GeoHierarchyNode geoHierarchyNode = geoHierarchyService.getHighestPostNode(enquiryOfficer.getPostGeoHierarchyNodeUuidMap());
            odApplication.setEnquiryOfficer(enquiryOfficer);
            odApplication.setGeoHierarchyNodeUuid(geoHierarchyNode.getUuid());
            odApplication.setStatus(OdApplicationStatus.ENQUIRY);
            odApplication.setEnquiryFilePath(null);
        }
        if (StringUtils.isNotEmpty(odApplicationPayload.getEnquiryFilePath())) {
            odApplication.setEnquiryFilePath(odApplicationPayload.getEnquiryFilePath());
            odApplication.setEnquirySubmittedAt(System.currentTimeMillis());
            odApplication.setStatus(OdApplicationStatus.REVIEW);
        }
        if (OdApplicationStatus.REVIEW.equals(odApplication.getStatus()) && OdApplicationStatus.ENQUIRY.equals(odApplicationPayload.getStatus())) {
            odApplication.setStatus(OdApplicationStatus.ENQUIRY);
            odApplication.setEnquiryFilePath(null);
        }
        if (OdApplicationStatus.REVIEW.equals(odApplication.getStatus()) && OdApplicationStatus.CLOSED.equals(odApplicationPayload.getStatus())) {
            odApplication.setStatus(OdApplicationStatus.CLOSED);
        }


        odApplication.setModifiedAt(System.currentTimeMillis());
        odApplicationRepository.save(odApplication);
        notificationService.sendNotification(odApplication);
        return odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build());
    }

    @LogPayload
    @Timed
    public OdApplicationPayload get(UUID odUuid, User principal) {
        OdApplication odApplication = odApplicationRepository.findByUuid(odUuid);
        return odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build());
    }

    @LogPayload
    @Timed
    public List<OdApplicationPayload> getList(String odApplicationStatus, User principal, List<UUID> geoHierarchyNodeUuids) {
        Map<Post, List<UUID>> geoNodes = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        OdApplicationStatus status = null;
        if (StringUtils.isNotEmpty(odApplicationStatus)) {
            status = OdApplicationStatus.valueOf(odApplicationStatus);
        }
        List<UUID> authorityNodes = geoHierarchyService.getAllLevelNodesOfAuthorityPost(geoNodes);
        List<OdApplication> result = new ArrayList<>();
        if (status != null) {
            if (CollectionUtils.isEmpty(authorityNodes)) {
                result = odApplicationRepository.findByOdOrEnquiryOfficerAndStatus(principal, status);
            } else {
                result = odApplicationRepository.findByGeoHierarchyNodeUuidInAndStatus(geoHierarchyService.getAllLevelNodesOfAuthorityPost(geoNodes), status);
            }
        } else {
            if (CollectionUtils.isEmpty(authorityNodes)) {
                result = odApplicationRepository.findByOdOrEnquiryOfficer(principal);
            } else {
                result = odApplicationRepository.findByGeoHierarchyNodeUuidIn(geoHierarchyService.getAllLevelNodesOfAuthorityPost(geoNodes));
            }
        }
        return result.stream()
                .map((odApplication) -> odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build()))
                .collect(Collectors.toList());

    }

    @LogPayload
    @Timed
    public List<OdApplicationPayload> getReceiptList(User principal, List<UUID> geoHierarchyNodeUuids) {
        Map<Post, List<UUID>> geoNodes = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        List<UUID> authorityNodes = geoHierarchyService.getAllLevelNodesOfAuthorityPost(geoNodes);
        if (CollectionUtils.isEmpty(authorityNodes)) {
            return odApplicationRepository.findByOd(principal)
                    .stream()
                    .map((odApplication) -> odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build()))
                    .collect(Collectors.toList());
        }
        return odApplicationRepository.findByGeoHierarchyNodeUuidIn(geoHierarchyService.getAllLevelNodesOfAuthorityPost(geoNodes))
                .stream()
                .map((odApplication) -> odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build()))
                .collect(Collectors.toList());

    }

    @LogPayload
    @Timed
    public AnalyticalResponse getDashboardAnalytics(User principal, List<UUID> geoHierarchyNodeUuids) {
        Map<Post, List<UUID>> geoNodes = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        List<UUID> authorityNodes = geoHierarchyService.getAllLevelNodesOfAuthorityPost(geoNodes);
        List<Object[]> allPostGeoAnalyticalRecord = new ArrayList<>();
        List<Object[]> selfAllPostGeoAnalyticalRecord = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(authorityNodes)) {
            allPostGeoAnalyticalRecord = odApplicationRepository.countByStatusForGeoNodes(authorityNodes);
        }
        selfAllPostGeoAnalyticalRecord= odApplicationRepository.countByStatusForOdOfficer(principal);
        Map<OdApplicationStatus, Long> geoStatusCountMap = allPostGeoAnalyticalRecord.stream()
                .filter(record -> !Objects.isNull(record[0]))
                .collect(Collectors.toMap(
                        record -> (OdApplicationStatus) record[0],  // status, which might be null
                        record -> (Long) record[1],                 // count
                        Long::sum                                  // in case of duplicate keys, sum the values
                ));
        Map<OdApplicationStatus, Long> selfStatusCountMap = selfAllPostGeoAnalyticalRecord.stream()
                .filter(record -> !Objects.isNull(record[0]))
                .collect(Collectors.toMap(
                        record -> (OdApplicationStatus) record[0],  // status, which might be null
                        record -> (Long) record[1],                 // count
                        Long::sum                                  // in case of duplicate keys, sum the values
                ));
        for(OdApplicationStatus odApplicationStatus: Arrays.asList(OdApplicationStatus.REVIEW, OdApplicationStatus.OPEN, OdApplicationStatus.CLOSED)) {
            selfStatusCountMap.put(odApplicationStatus, geoStatusCountMap.getOrDefault(odApplicationStatus,0L));
        }
        return AnalyticalResponse.builder()
                .statusCountMap(geoStatusCountMap)
                .self_statusCountMap(selfStatusCountMap)
                .build();
    }
}
