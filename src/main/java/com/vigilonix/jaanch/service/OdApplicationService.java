package com.vigilonix.jaanch.service;

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
    private final FieldGeoService fieldGeoService;
    private final ValidationService<ODApplicationValidationPayload> odUpdateValidationService;
    private final ValidationService<ODApplicationValidationPayload> odCreateValidationService;
    private final NotificationService notificationService;

    @Autowired
    public OdApplicationService(
            OdApplicationRepository odApplicationRepository,
            OdApplicationTransformer odApplicationTransformer,
            UserRepository userRepository,
            FieldGeoService fieldGeoService,
            @Qualifier("update") ValidationService<ODApplicationValidationPayload> odUpdateValidationService,
            @Qualifier("create") ValidationService<ODApplicationValidationPayload> odCreateValidationService, NotificationService notificationService) {
        this.odApplicationRepository = odApplicationRepository;
        this.odApplicationTransformer = odApplicationTransformer;
        this.userRepository = userRepository;
        this.fieldGeoService = fieldGeoService;
        this.odUpdateValidationService = odUpdateValidationService;
        this.odCreateValidationService = odCreateValidationService;
        this.notificationService = notificationService;
    }

    public OdApplicationPayload create(OdApplicationPayload odApplicationPayload, User principal) {
        odCreateValidationService.validate(ODApplicationValidationPayload.builder().odApplicationPayload(odApplicationPayload).principalUser(principal).build());

        Long epoch = System.currentTimeMillis();
        FieldGeoNode fieldGeoNode = Objects.isNull(odApplicationPayload.getFieldGeoNodeUuid())
                ? fieldGeoService.resolveFieldGeoNode(principal.getPostFieldGeoNodeUuidMap())
                : fieldGeoService.getFieldGeoNode(odApplicationPayload.getFieldGeoNodeUuid());
        Optional<Integer> maxBucketNo = odApplicationRepository.findMaxReceiptBucketNumberForCurrentMonth(fieldGeoNode.getUuid());
        int bucketNo = maxBucketNo.map(integer -> integer + 1).orElse(1);

        OdApplication odApplication = OdApplication.builder()
                .fieldGeoNodeUuid(fieldGeoNode.getUuid())
                .uuid(UUID.randomUUID())
                .od(principal)
                .applicantName(odApplicationPayload.getApplicantName())
                .applicationFilePath(odApplicationPayload.getApplicationFilePath())
                .applicantPhoneNumber(odApplicationPayload.getApplicantPhoneNumber())
                .receiptNo(generateReceiptNumber(fieldGeoNode, bucketNo))
                .receiptBucketNumber(bucketNo)
                .fieldGeoNodeUuid(fieldGeoNode.getUuid())
                .createdAt(epoch)
                .modifiedAt(epoch)
                .status(ODApplicationStatus.OPEN)
                .build();
        odApplicationRepository.save(odApplication);
        return odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build());
    }

    private String generateReceiptNumber(FieldGeoNode fieldGeoNode, int bucketNo) {
        String jurisdictionName = fieldGeoNode.getName().replace("  ", " ")
                .replace(" ", "_");
        // Get the current date
        LocalDate currentDate = LocalDate.now();
        // Define the date formatter with the desired pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
        // Format the current date
        String formattedDate = currentDate.format(formatter);
        return String.format("%s_%s_%s", jurisdictionName, formattedDate, bucketNo);
    }

    public OdApplicationPayload update(UUID uuid, OdApplicationPayload odApplicationPayload, User principal) {
        OdApplication odApplication = odApplicationRepository.findByUuid(uuid);
        odUpdateValidationService.validate(ODApplicationValidationPayload.builder()
                .odApplicationPayload(odApplicationPayload)
                .odApplication(odApplication)
                .enquiryUser(userRepository.findByUuid(odApplicationPayload.getEnquiryOfficerUuid()))
                .principalUser(principal).build());
        if (!Objects.isNull(odApplicationPayload.getEnquiryOfficerUuid())) {
            User enquiryOfficer = userRepository.findByUuid(odApplicationPayload.getEnquiryOfficerUuid());
            FieldGeoNode fieldGeoNode = fieldGeoService.resolveFieldGeoNode(enquiryOfficer.getPostFieldGeoNodeUuidMap());
            odApplication.setEnquiryOfficer(enquiryOfficer);
            odApplication.setFieldGeoNodeUuid(fieldGeoNode.getUuid());
            odApplication.setEnquirySubmittedAt(System.currentTimeMillis());
            odApplication.setStatus(ODApplicationStatus.ENQUIRY);
        }
        if (StringUtils.isNotEmpty(odApplicationPayload.getEnquiryFilePath())) {
            odApplication.setEnquiryFilePath(odApplicationPayload.getEnquiryFilePath());
            odApplication.setStatus(ODApplicationStatus.REVIEW);
        }
        if (ODApplicationStatus.REVIEW.equals(odApplication.getStatus()) && ODApplicationStatus.ENQUIRY.equals(odApplicationPayload.getStatus())) {
            odApplication.setStatus(ODApplicationStatus.ENQUIRY);
        }
        if (ODApplicationStatus.REVIEW.equals(odApplication.getStatus()) && ODApplicationStatus.CLOSED.equals(odApplicationPayload.getStatus())) {
            odApplication.setStatus(ODApplicationStatus.CLOSED);
        }


        odApplication.setModifiedAt(System.currentTimeMillis());
        odApplicationRepository.save(odApplication);
        return odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build());
    }

    public OdApplicationPayload get(UUID odUuid, User principal) {
        OdApplication odApplication = odApplicationRepository.findByUuid(odUuid);
        return odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build());
    }

    public List<OdApplicationPayload> getList(String odApplicationStatus, User principal) {
        ODApplicationStatus status = null;
        if (StringUtils.isNotEmpty(odApplicationStatus)) {
            status = ODApplicationStatus.valueOf(odApplicationStatus);
        }
        List<FieldGeoNode> fieldNodes = fieldGeoService.getOwnershipGeoNodes(principal.getPostFieldGeoNodeUuidMap());
        List<OdApplication> result = new ArrayList<>();
        if (status != null) {
            if (CollectionUtils.isEmpty(fieldNodes)) {
                result = odApplicationRepository.findByOdOrEnquiryOfficerAndStatus(principal, status);
            } else {
                result = odApplicationRepository.findByFieldGeoNodeUuidInAndStatus(fieldGeoService.getAllOwnershipChildren(principal.getPostFieldGeoNodeUuidMap()), status);
            }
        } else {
            if (CollectionUtils.isEmpty(fieldNodes)) {
                result = odApplicationRepository.findByOdOrEnquiryOfficer(principal);
            } else {
                result = odApplicationRepository.findByFieldGeoNodeUuidIn(fieldGeoService.getAllOwnershipChildren(principal.getPostFieldGeoNodeUuidMap()));
            }
        }
        return result.stream()
                .map((odApplication) -> odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build()))
                .collect(Collectors.toList());

    }

    public List<OdApplicationPayload> getReceiptList(User principal) {
        List<FieldGeoNode> fieldNodes = fieldGeoService.getOwnershipGeoNodes(principal.getPostFieldGeoNodeUuidMap());
        if (CollectionUtils.isEmpty(fieldNodes)) {
            return odApplicationRepository.findByOd(principal)
                    .stream()
                    .map((odApplication) -> odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build()))
                    .collect(Collectors.toList());
        }
        return odApplicationRepository.findByFieldGeoNodeUuidIn(fieldGeoService.getAllOwnershipChildren(principal.getPostFieldGeoNodeUuidMap()))
                .stream()
                .map((odApplication) -> odApplicationTransformer.transform(ODApplicationTransformationRequest.builder().odApplication(odApplication).principalUser(principal).build()))
                .collect(Collectors.toList());

    }
}
