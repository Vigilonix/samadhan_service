package com.vigilonix.samadhan.service;

import com.vigilonix.samadhan.aop.LogPayload;
import com.vigilonix.samadhan.aop.Timed;
import com.vigilonix.samadhan.enums.*;
import com.vigilonix.samadhan.exception.ValidationRuntimeException;
import com.vigilonix.samadhan.pojo.OdApplicationFilterRequest;
import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.OdApplicationAssignment;
import com.vigilonix.samadhan.model.OdApplicationAssignmentHistory;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.pojo.*;
import com.vigilonix.samadhan.pojo.whatsapp.ODApplicationAssignmentTransformationRequest;
import com.vigilonix.samadhan.repository.*;
import com.vigilonix.samadhan.transformer.OdApplicationAssignmentTransformer;
import com.vigilonix.samadhan.transformer.OdApplicationTransformer;
import com.vigilonix.samadhan.validator.ValidationService;
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
    private final UserRepositoryCustom userRepositoryCustom;
    private final GeoHierarchyService geoHierarchyService;
    private final ValidationService<ODApplicationValidationPayload> odUpdateValidationService;
    private final ValidationService<ODApplicationValidationPayload> odCreateValidationService;
    private final NotificationService notificationService;
    private final OdApplicationAssignmentRepository odApplicationAssignmentRepository;
    private final OdApplicationAssignmentHistoryRepository odApplicationAssignmentHistoryRepository;
    private final OdApplicationAssignmentTransformer odApplicationAssignmentTransformer;

    @Autowired
    public OdApplicationService(
            OdApplicationRepository odApplicationRepository,
            OdApplicationTransformer odApplicationTransformer,
            UserRepositoryCustom userRepositoryCustom, GeoHierarchyService geoHierarchyService,
            @Qualifier("update") ValidationService<ODApplicationValidationPayload> odUpdateValidationService,
            @Qualifier("create") ValidationService<ODApplicationValidationPayload> odCreateValidationService, NotificationService notificationService, OdApplicationAssignmentRepository odApplicationAssignmentRepository, OdApplicationAssignmentHistoryRepository odApplicationAssignmentHistoryRepository, OdApplicationAssignmentTransformer odApplicationAssignmentTransformer) {
        this.odApplicationRepository = odApplicationRepository;
        this.odApplicationTransformer = odApplicationTransformer;
        this.userRepositoryCustom = userRepositoryCustom;
        this.geoHierarchyService = geoHierarchyService;
        this.odUpdateValidationService = odUpdateValidationService;
        this.odCreateValidationService = odCreateValidationService;
        this.notificationService = notificationService;
        this.odApplicationAssignmentRepository = odApplicationAssignmentRepository;
        this.odApplicationAssignmentHistoryRepository = odApplicationAssignmentHistoryRepository;
        this.odApplicationAssignmentTransformer = odApplicationAssignmentTransformer;
    }

    @Timed
    @LogPayload
    public OdApplicationPayload create(OdApplicationPayload odApplicationPayload, User principal, List<UUID> geoHierarchyNodeUuids) {
        odCreateValidationService.validate(ODApplicationValidationPayload.builder()
                .odApplicationPayload(odApplicationPayload).principalUser(principal)
                .geoHierarchyNodeUuids(geoHierarchyNodeUuids)
                .build());

        UUID applicationUuid = UUID.randomUUID();
        Long epoch = System.currentTimeMillis();


        OdApplication.OdApplicationBuilder odApplicationBuilder = OdApplication.builder()
                .uuid(applicationUuid)
                .od(principal)
                .applicantName(odApplicationPayload.getApplicantName())
                .applicationFilePath(odApplicationPayload.getApplicationFilePath())
                .applicantPhoneNumber(odApplicationPayload.getApplicantPhoneNumber())
                .createdAt(epoch)
                .modifiedAt(epoch)
                .status(OdApplicationStatus.OPEN)
                .category(odApplicationPayload.getCategory())
                .dueEpoch(odApplicationPayload.getDueEpoch())
                .category(odApplicationPayload.getCategory())
                .priority(odApplicationPayload.getApplicationPriority()==null?ApplicationPriority.MEDIUM:odApplicationPayload.getApplicationPriority())
                .comment(odApplicationPayload.getComment());

        GeoHierarchyNode geoHierarchyNode = resolveGeoHierarchyNode(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);

        if(odApplicationPayload.getParentAssignmentUuid()!=null) {
            OdApplicationAssignment parentAssignment = odApplicationAssignmentRepository.findByUuid(odApplicationPayload.getParentAssignmentUuid());
            if(parentAssignment!=null) {
                odApplicationBuilder.parentApplicationUuid(parentAssignment.getApplication().getUuid())
                .parentAssignmentUuid(parentAssignment.getUuid())
                .od(principal)
                .applicantName(parentAssignment.getApplication().getApplicantName())
                .applicationFilePath(parentAssignment.getApplication().getApplicationFilePath())
                .applicantPhoneNumber(parentAssignment.getApplication().getApplicantPhoneNumber())
                .createdAt(epoch)
                .modifiedAt(epoch)
                .status(OdApplicationStatus.OPEN)
                .dueEpoch(parentAssignment.getApplication().getDueEpoch())
                .priority(parentAssignment.getApplication().getPriority())
                .category(parentAssignment.getApplication().getCategory());

                geoHierarchyNode = geoHierarchyService.getNodeById(parentAssignment.getGeoHierarchyNodeUuid());
                parentAssignment.setChildApplicationUuid(applicationUuid);
                odApplicationAssignmentRepository.save(parentAssignment);
            } else {
                throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_ID));
            }
        }

        Optional<Integer> maxBucketNo = odApplicationRepository.findMaxReceiptBucketNumberForCurrentMonth(geoHierarchyNode.getUuid());
        int bucketNo = maxBucketNo.map(integer -> integer + 1).orElse(1);

        OdApplication odApplication = odApplicationBuilder
                .geoHierarchyNodeUuid(geoHierarchyNode.getUuid())
                .receiptNo(generateReceiptNumber(geoHierarchyNode, bucketNo))
                .receiptBucketNumber(bucketNo)
                .build();
        odApplicationRepository.save(odApplication);
        notificationService.sendNotification(odApplication);
        return odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build());
    }

    private GeoHierarchyNode resolveGeoHierarchyNode(Map<Post, List<UUID>> postGeoHierarchyNodeUuidMap, List<UUID> geoHierarchyNodeUuids) {
        return CollectionUtils.isEmpty(geoHierarchyNodeUuids)
                ? geoHierarchyService.getHighestPostNode(postGeoHierarchyNodeUuidMap)
                : geoHierarchyService.getNodeById(geoHierarchyNodeUuids.get(0));
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
//        odUpdateValidationService.validate(ODApplicationValidationPayload.builder()
//                .odApplicationPayload(odApplicationPayload)
//                .odApplication(odApplication)
//                .enquiryUser(userRepository.findByUuid(odApplicationPayload.getEnquiryOfficerUuid()))
//                .principalUser(principal).build());

//        if (CollectionUtils.isNotEmpty(odApplicationPayload.getEnquiries())) {
//            List<Enquiry> enquiries = odApplicationPayload.getEnquiries().stream().map(e-> Enquiry.builder().build()).collect(Collectors.toList());
//            enquiries.addAll(odApplication.getEnquiries());
//            odApplication.setEnquiries(enquiries);
//            odApplication.setStatus(OdApplicationStatus.REVIEW);
//        }
//        if (OdApplicationStatus.REVIEW.equals(odApplication.getStatus()) && OdApplicationStatus.ENQUIRY.equals(odApplicationPayload.getStatus())) {
//            odApplication.setStatus(OdApplicationStatus.ENQUIRY);
//        }
        if (OdApplicationStatus.CLOSED.equals(odApplicationPayload.getStatus())) {
            odApplication.setStatus(OdApplicationStatus.CLOSED);
            log.info("closing {}", odApplication);
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
        List<ODApplicationAssignmentTransformationRequest> assignments = odApplicationAssignmentRepository.findLatestAssignmentForEachAssignee(odApplication)
                .stream()
                .map(a -> ODApplicationAssignmentTransformationRequest.builder()
                        .assignment(a)
                        .principalUser(principal)
                        .enquiryUser(userRepositoryCustom.findAuthorityGeoHierarchyUser(a.getGeoHierarchyNodeUuid()))
                        .build())
                .collect(Collectors.toList());
        return odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).assignments(assignments).build());
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
                result = odApplicationRepository.findByOdOrEnquiryOfficerAndStatus(principal, status, geoHierarchyService.getAllLevelNodes(principal.getPostGeoHierarchyNodeUuidMap()));
            } else {
                result = odApplicationRepository.findByGeoHierarchyNodeUuidInAndStatus(geoHierarchyService.getAllLevelNodesOfAuthorityPost(geoNodes), status);
            }
        } else {
            if (CollectionUtils.isEmpty(authorityNodes)) {
                result = odApplicationRepository.findByOdOrEnquiryOfficer(principal, geoHierarchyService.getAllLevelNodes(principal.getPostGeoHierarchyNodeUuidMap()));
            } else {
                result = odApplicationRepository.findByGeoHierarchyNodeUuidIn(geoHierarchyService.getAllLevelNodesOfAuthorityPost(geoNodes));
            }
        }
        return result.stream()
                .map((odApplication) -> {
                    List<ODApplicationAssignmentTransformationRequest> assignments = odApplicationAssignmentRepository.findLatestAssignmentForEachAssignee(odApplication)
                            .stream()
                            .map(a -> ODApplicationAssignmentTransformationRequest.builder()
                                    .assignment(a)
                                    .principalUser(principal)
                                    .enquiryUser(userRepositoryCustom.findAuthorityGeoHierarchyUser(a.getGeoHierarchyNodeUuid()))
                                    .build())
                            .collect(Collectors.toList());
                    return odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().assignments(assignments).odApplication(odApplication).principalUser(principal).build());
                })
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
        List<Object[]> allPostGeoAnalyticalRecord  = odApplicationRepository.applicationStatusCountByAssignmentGeoFilter(geoHierarchyService.getFirstLevelNodes(geoNodes));
        Map<OdApplicationStatus, Long> geoStatusCountMap = allPostGeoAnalyticalRecord.stream()
                .filter(record -> !Objects.isNull(record[0]))
                .collect(Collectors.toMap(
                        record -> (OdApplicationStatus) record[0],  // status, which might be null
                        record -> (Long) record[1],                 // count
                        Long::sum                                  // in case of duplicate keys, sum the values
                ));

        List<Object[]>  selfAllPostGeoAnalyticalRecord =  odApplicationRepository.applicationStatusCountBytGeoFilter(geoHierarchyService.getFirstLevelNodes(geoNodes));
        log.info("self map out put size {} dump {}", selfAllPostGeoAnalyticalRecord.size(), selfAllPostGeoAnalyticalRecord);
        Map<OdApplicationStatus, Long> selfStatusCountMap = selfAllPostGeoAnalyticalRecord.stream()
                .filter(record -> !Objects.isNull(record[0]))
                .collect(Collectors.toMap(
                        record -> (OdApplicationStatus) record[0],  // status, which might be null
                        record -> (Long) record[1],                 // count
                        Long::sum                                  // in case of duplicate keys, sum the values
                ));
        log.info("transformed status map {}", selfStatusCountMap);
        for (OdApplicationStatus odApplicationStatus : Arrays.asList(OdApplicationStatus.REVIEW, OdApplicationStatus.OPEN, OdApplicationStatus.CLOSED)) {
            selfStatusCountMap.put(odApplicationStatus, selfStatusCountMap.getOrDefault(odApplicationStatus, 0L));
        }
        log.info("analytics output for user {} geonodes {} self {} geo {} input list {}", principal.getUuid(), geoNodes, selfStatusCountMap, geoStatusCountMap, geoHierarchyService.getFirstLevelNodes(geoNodes));
        return AnalyticalResponse.builder()
                .statusCountMap(geoStatusCountMap)
                .selfStatusCountMap(selfStatusCountMap)
                .build();
    }

    public String getAnalytics(User principal, List<UUID> geoHierarchyNodeUuids) {
        Map<Post, List<UUID>> geoNodes = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        return """
                                
                """;
    }

    public void createAssignment(List<OdAssignmentPayload> assignmentRequests, UUID odApplicationUuid, User principal, List<UUID> geoHierarchyNodeUuids) {
        OdApplication odApplication = odApplicationRepository.findByUuid(odApplicationUuid);
        GeoHierarchyNode geoHierarchyNode = resolveGeoHierarchyNode(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        for (OdAssignmentPayload assignmentPojo : assignmentRequests) {
//            User assignee = userRepository.findByUuid(assignmentPojo.getAssigneeUuid());
            OdApplicationAssignment odApplicationAssignment = OdApplicationAssignment.builder()
                    .uuid(UUID.randomUUID())
                    .application(odApplication)
//                    .enquiryOfficer(assignee)
                    .actor(principal)
                    .createdAt(System.currentTimeMillis())
                    .modifiedAt(System.currentTimeMillis())
                    .status(OdApplicationStatus.ENQUIRY)
                    .geoHierarchyNodeUuid(assignmentPojo.getGeoHierarchyNodeUuid())
                    .build();
            odApplicationAssignmentRepository.save(odApplicationAssignment);

            OdApplicationAssignmentHistory odApplicationAssignmentHistory = getOdApplicationAssignmentHistory(odApplicationAssignment, ActorType.SYSTEM);
            odApplicationAssignmentHistoryRepository.save(odApplicationAssignmentHistory);
        }
        odApplication.setStatus(OdApplicationStatus.ENQUIRY);
        odApplicationRepository.save(odApplication);
    }

    public OdAssignmentPayload updateAssignment(OdAssignmentPayload assignmentPayload, UUID assignmentUuid, User principal) {
        OdApplicationAssignment odApplicationAssignment = odApplicationAssignmentRepository.findByUuid(assignmentUuid);
        ActorType actorType = geoHierarchyService.hasAuthority(odApplicationAssignment.getApplication().getGeoHierarchyNodeUuid(), principal.getPostGeoHierarchyNodeUuidMap()) ? ActorType.APPLICATION : ActorType.ASSIGNMENT;

        if(StringUtils.isNotEmpty(assignmentPayload.getComment())) {
            odApplicationAssignment.setComment(assignmentPayload.getComment());
        }
        if (OdApplicationStatus.REVIEW.equals(odApplicationAssignment.getStatus()) && OdApplicationStatus.ENQUIRY.equals(assignmentPayload.getStatus())) {
            actorType = ActorType.APPLICATION;
            odApplicationAssignment.setStatus(OdApplicationStatus.ENQUIRY);
            odApplicationAssignment.setFilePath(assignmentPayload.getFilePath());
        }
        else if (OdApplicationStatus.REVIEW.equals(odApplicationAssignment.getStatus()) && OdApplicationStatus.CLOSED.equals(assignmentPayload.getStatus())) {
            actorType = ActorType.APPLICATION;
            odApplicationAssignment.setStatus(OdApplicationStatus.CLOSED);
        }
        else if (OdApplicationStatus.ENQUIRY.equals(odApplicationAssignment.getStatus()) && OdApplicationStatus.REVIEW.equals(assignmentPayload.getStatus())) {
            actorType = ActorType.ASSIGNMENT;
            odApplicationAssignment.setFilePath(assignmentPayload.getFilePath());
            odApplicationAssignment.setStatus(OdApplicationStatus.REVIEW);
        }
        odApplicationAssignment.setModifiedAt(System.currentTimeMillis());
        odApplicationAssignment.setActor(principal);
        odApplicationAssignmentRepository.save(odApplicationAssignment);

        OdApplicationAssignmentHistory odApplicationAssignmentHistory = getOdApplicationAssignmentHistory(odApplicationAssignment, actorType);
        odApplicationAssignmentHistoryRepository.save(odApplicationAssignmentHistory);

        updateApplicationStatus(odApplicationAssignment.getApplication());

        return odApplicationAssignmentTransformer.transform(ODApplicationAssignmentTransformationRequest
                .builder()
                .assignment(odApplicationAssignment)
                .enquiryUser(userRepositoryCustom.findAuthorityGeoHierarchyUser(odApplicationAssignment.getGeoHierarchyNodeUuid()))
                .principalUser(principal)
                .build());
    }

    private void updateApplicationStatus(OdApplication odApplication) {
        List<OdApplicationAssignment> assignments = odApplicationAssignmentRepository.findByApplication(odApplication);

        boolean isEnquiry = assignments.stream()
                .anyMatch(a -> OdApplicationStatus.ENQUIRY.equals(a.getStatus()));
        boolean isReview = assignments.stream()
                .allMatch(a -> OdApplicationStatus.REVIEW.equals(a.getStatus()));
        boolean isClosed = assignments.stream()
                .allMatch(a -> OdApplicationStatus.CLOSED.equals(a.getStatus()));

        if (isEnquiry) {
            odApplication.setStatus(OdApplicationStatus.ENQUIRY);
        } else if (isReview) {
            odApplication.setStatus(OdApplicationStatus.REVIEW);
        } else if (isClosed) {
            odApplication.setStatus(OdApplicationStatus.CLOSED);
        }

        odApplicationRepository.save(odApplication);
    }

    private OdApplicationAssignmentHistory getOdApplicationAssignmentHistory(OdApplicationAssignment odApplicationAssignment, ActorType actorType) {
        OdApplicationAssignmentHistory odApplicationAssignmentHistory = OdApplicationAssignmentHistory.builder()
                .uuid(UUID.randomUUID())
                .assignmentUuid(odApplicationAssignment.getUuid())
                .comment(odApplicationAssignment.getComment())
                .filePath(odApplicationAssignment.getFilePath())
                .createdAt(odApplicationAssignment.getCreatedAt())
                .modifiedAt(odApplicationAssignment.getModifiedAt())
                .status(odApplicationAssignment.getStatus())
                .geoHierarchyNodeUuid(odApplicationAssignment.getGeoHierarchyNodeUuid())
                .actor(odApplicationAssignment.getActor())
                .actorType(actorType)
                .build();
        return odApplicationAssignmentHistory;
    }

    public List<OdAssignmentPayload> getAssignmentHistory(UUID assignmentUuid, User principal) {
        return odApplicationAssignmentHistoryRepository.findByAssignmentUuid(assignmentUuid).stream()
                .map(h-> getOdAssignmentPayload(h))
                .collect(Collectors.toList());
    }

    private OdAssignmentPayload getOdAssignmentPayload(OdApplicationAssignmentHistory h) {
        return OdAssignmentPayload.builder()
                .uuid(h.getUuid())
                .assigneeUuid(h.getAssignmentUuid())
                .modifiedAt(h.getModifiedAt())
                .comment(h.getComment())
                .filePath(h.getFilePath())
                .createdAt(h.getCreatedAt())
                .status(h.getStatus())
                .actorUuid(h.getActor().getUuid())
                .actorName(h.getActor().getName())
                .geoHierarchyNodeName(geoHierarchyService.getNodeById(h.getGeoHierarchyNodeUuid()).getName())
                .geoHierarchyNodeUuid(h.getGeoHierarchyNodeUuid())
                .actorType(h.getActorType())
                .build();
    }

    public OdApplicationFilterResponse getFilteredList(User principal, OdApplicationFilterRequest odApplicationFilterRequest, List<UUID> geoHierarchyNodeUuids) {
        Map<Post, List<UUID>> postGeoHierarchyNodeMap= geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        boolean isAuthority = !CollectionUtils.isEmpty(geoHierarchyService.getAllLevelNodesOfAuthorityPost(postGeoHierarchyNodeMap));
        List<UUID> geoNodes = geoHierarchyService.getFirstLevelNodes(postGeoHierarchyNodeMap);
        List<UUID> filterGeoNodes = CollectionUtils.isEmpty(odApplicationFilterRequest.getGeoHierarchyNodeUuids())
                ? geoHierarchyService.getAllLevelNodes(postGeoHierarchyNodeMap)
                : odApplicationFilterRequest.getGeoHierarchyNodeUuids();
        List<ApplicationCategory> categories = odApplicationFilterRequest.getCategories();
        Map<ApplicationFilterRequestStatus, List<OdApplicationPayload>> resultMap = new HashMap<>() ;
        List<OdApplication> response = new ArrayList<>();
        String searchKeyword = StringUtils.isEmpty(odApplicationFilterRequest.getSearchKeyword())? "":StringUtils.strip(odApplicationFilterRequest.getSearchKeyword()).toLowerCase();
        if(!isAuthority) {
            response = odApplicationRepository
                    .findByOdAndGeoHierarchyNodeUuidIn(principal, geoNodes);
        }
        else if(ApplicationFilterRequestStatus.ENQUIRY.equals(odApplicationFilterRequest.getStatus())) {
            response = odApplicationRepository
                    .findByCategoryInAndAssignmentStatusAndAssignmentGeoHierarchyNodeUuidIn(categories, geoNodes, OdApplicationStatus.ENQUIRY, searchKeyword, filterGeoNodes);
        }
        else if(ApplicationFilterRequestStatus.PENDING_ENQUIRY.equals(odApplicationFilterRequest.getStatus())) {
            response = odApplicationRepository
                    .findByCategoryInAndAssignmentStatusAndGeoHierarchyNodeUuidIn(categories, geoNodes, OdApplicationStatus.ENQUIRY, searchKeyword, filterGeoNodes);
        }
        else if(ApplicationFilterRequestStatus.REVIEW.equals(odApplicationFilterRequest.getStatus())) {
            response =odApplicationRepository
                    .findByCategoryInAndAssignmentStatusAndGeoHierarchyNodeUuidIn(categories, geoNodes, OdApplicationStatus.REVIEW, searchKeyword, filterGeoNodes);
        }
        else if(ApplicationFilterRequestStatus.PENDING_REVIEW.equals(odApplicationFilterRequest.getStatus())) {
            response = odApplicationRepository
                    .findByCategoryInAndAssignmentStatusAndAssignmentGeoHierarchyNodeUuidIn(categories, geoNodes, OdApplicationStatus.ENQUIRY, searchKeyword, filterGeoNodes);
        }
        else if(ApplicationFilterRequestStatus.OPEN.equals(odApplicationFilterRequest.getStatus())) {
            response = odApplicationRepository
                    .findByStatusAndCategoryInAndGeoHierarchyNodeUuidIn(OdApplicationStatus.OPEN, categories, geoNodes, searchKeyword, filterGeoNodes);
        }
        else if(ApplicationFilterRequestStatus.CLOSED.equals(odApplicationFilterRequest.getStatus())) {
            response = odApplicationRepository
                    .findByStatusAndCategoryInAndGeoHierarchyNodeUuidIn(OdApplicationStatus.CLOSED, categories, geoNodes, searchKeyword, filterGeoNodes);
        }else if(ApplicationFilterRequestStatus.ALL.equals(odApplicationFilterRequest.getStatus())) {
            response = odApplicationRepository
                    .findByCategoryInAndGeoHierarchyNodeUuidIn(categories, geoNodes, searchKeyword, filterGeoNodes);
        }
        return OdApplicationFilterResponse.builder()
                .applications(transformApplication(response, principal))
                .build();
    }

    public AnalyticalResponse getAnalyticaCount(User principal, OdApplicationFilterRequest odApplicationFilterRequest, List<UUID> geoHierarchyNodeUuids) {
        Map<ApplicationFilterRequestStatus, Integer> resultMap = new HashMap<>();
        for(ApplicationFilterRequestStatus applicationFilterRequestStatus : ApplicationFilterRequestStatus.values()) {
            resultMap.put(applicationFilterRequestStatus, getFilteredCount(principal, OdApplicationFilterRequest.builder()
                    .categories(odApplicationFilterRequest.getCategories())
                    .searchKeyword(odApplicationFilterRequest.getSearchKeyword())
                    .geoHierarchyNodeUuids(odApplicationFilterRequest.getGeoHierarchyNodeUuids())
                    .status(applicationFilterRequestStatus)
                    .build(), geoHierarchyNodeUuids));
        }
        log.info("fauload for request geo {} {} is {}", geoHierarchyNodeUuids, odApplicationFilterRequest, resultMap);
        return AnalyticalResponse.builder()
                .requestStatusCountMap(resultMap)
                .build();
    }

    public Integer getFilteredCount(User principal, OdApplicationFilterRequest odApplicationFilterRequest, List<UUID> geoHierarchyNodeUuids) {
        Map<Post, List<UUID>> postGeoHierarchyNodeMap= geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        boolean isAuthority = !CollectionUtils.isEmpty(geoHierarchyService.getAllLevelNodesOfAuthorityPost(postGeoHierarchyNodeMap));
        List<UUID> geoNodes = geoHierarchyService.getFirstLevelNodes(postGeoHierarchyNodeMap);
        List<UUID> filterGeoNodes = CollectionUtils.isEmpty(odApplicationFilterRequest.getGeoHierarchyNodeUuids())
                ? geoHierarchyService.getAllLevelNodes(postGeoHierarchyNodeMap)
                : odApplicationFilterRequest.getGeoHierarchyNodeUuids();
        List<ApplicationCategory> categories = odApplicationFilterRequest.getCategories();
        Map<ApplicationFilterRequestStatus, List<OdApplicationPayload>> resultMap = new HashMap<>() ;
        List<OdApplication> response = new ArrayList<>();
        String searchKeyword = StringUtils.isEmpty(odApplicationFilterRequest.getSearchKeyword())? "":StringUtils.strip(odApplicationFilterRequest.getSearchKeyword()).toLowerCase();

        if(!isAuthority) {
            return odApplicationRepository
                    .countByOdAndGeoHierarchyNodeUuidIn(principal, geoNodes);
        }
        else if(ApplicationFilterRequestStatus.ENQUIRY.equals(odApplicationFilterRequest.getStatus())) {
            return odApplicationRepository
                    .countByCategoryInAndAssignmentStatusAndAssignmentGeoHierarchyNodeUuidIn(categories, geoNodes, OdApplicationStatus.ENQUIRY, searchKeyword, filterGeoNodes);
        }
        else if(ApplicationFilterRequestStatus.PENDING_ENQUIRY.equals(odApplicationFilterRequest.getStatus())) {
            return odApplicationRepository
                    .countByCategoryInAndAssignmentStatusAndGeoHierarchyNodeUuidIn(categories, geoNodes, OdApplicationStatus.ENQUIRY, searchKeyword, filterGeoNodes);
        }
        else if(ApplicationFilterRequestStatus.REVIEW.equals(odApplicationFilterRequest.getStatus())) {
            return odApplicationRepository
                    .countByCategoryInAndAssignmentStatusAndGeoHierarchyNodeUuidIn(categories, geoNodes, OdApplicationStatus.REVIEW, searchKeyword, filterGeoNodes);
        }
        else if(ApplicationFilterRequestStatus.PENDING_REVIEW.equals(odApplicationFilterRequest.getStatus())) {
            return odApplicationRepository
                    .countByCategoryInAndAssignmentStatusAndAssignmentGeoHierarchyNodeUuidIn(categories, geoNodes, OdApplicationStatus.ENQUIRY, searchKeyword, filterGeoNodes);
        }
        else if(ApplicationFilterRequestStatus.OPEN.equals(odApplicationFilterRequest.getStatus())) {
            return odApplicationRepository
                    .countByStatusAndCategoryInAndGeoHierarchyNodeUuidIn(OdApplicationStatus.OPEN, categories, geoNodes, searchKeyword, filterGeoNodes);
        }
        else if(ApplicationFilterRequestStatus.CLOSED.equals(odApplicationFilterRequest.getStatus())) {
            return odApplicationRepository
                    .countByStatusAndCategoryInAndGeoHierarchyNodeUuidIn(OdApplicationStatus.CLOSED, categories, geoNodes, searchKeyword, filterGeoNodes);
        }else if(ApplicationFilterRequestStatus.ALL.equals(odApplicationFilterRequest.getStatus())) {
            return odApplicationRepository
                    .countByCategoryInAndGeoHierarchyNodeUuidIn(categories, geoNodes, searchKeyword, filterGeoNodes);
        }
        return 0;
    }

    private List<OdApplicationPayload> transformApplication(List<OdApplication> odApplications, User principal) {
        return odApplications.stream()
                .map((odApplication) -> {
                            List<ODApplicationAssignmentTransformationRequest> assignments = odApplicationAssignmentRepository.findLatestAssignmentForEachAssignee(odApplication)
                                    .stream()
                                    .map(a -> ODApplicationAssignmentTransformationRequest.builder()
                                            .assignment(a)
                                            .principalUser(principal)
                                            .enquiryUser(userRepositoryCustom.findAuthorityGeoHierarchyUser(a.getGeoHierarchyNodeUuid()))
                                            .build())
                                    .collect(Collectors.toList());
                            return odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().assignments(assignments).odApplication(odApplication).principalUser(principal).build());
                        }
                ).collect(Collectors.toList());
    }
}