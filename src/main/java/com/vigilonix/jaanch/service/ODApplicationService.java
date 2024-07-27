package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.enums.Post;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
import com.vigilonix.jaanch.exception.ValidationRuntimeException;
import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.pojo.FieldGeoNode;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.ODApplicationPojo;
import com.vigilonix.jaanch.pojo.ODApplicationStatus;
import com.vigilonix.jaanch.repository.OdApplicationRepository;
import com.vigilonix.jaanch.repository.UserRepository;
import com.vigilonix.jaanch.transformer.OdApplicationTransformer;
import com.vigilonix.jaanch.validator.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ODApplicationService {
    private final OdApplicationRepository odApplicationRepository;
    private final OdApplicationTransformer odApplicationTransformer;
    private final UserRepository userRepository;
    private final FieldGeoService fieldGeoService;
//    private final ValidationService<ODApplicationPojo> OdApplicationValidatorService;
    private final UserService userService;

    public ODApplicationPojo create(ODApplicationPojo odApplicationPojo, User principal) {
//        OdApplicationValidatorService.validate(odApplicationPojo);
        FieldGeoNode fieldGeoNode = fieldGeoService.resolveFieldGeoNode(principal.getPostFieldGeoNodeUuidMap());
        OdApplication odApplication = OdApplication.builder()
                .fieldGeoNodeUuid(fieldGeoNode.getUuid())
                .uuid(UUID.randomUUID())
                .odUuid(principal.getUuid())
                .applicantName(odApplicationPojo.getApplicantName())
                .applicationFilePath(odApplicationPojo.getApplicationFilePath())
                .applicantPhoneNumber(odApplicationPojo.getApplicantPhoneNumber())
                .receiptNo(generateReceiptNumber(principal.getPostFieldGeoNodeUuidMap()))
                .fieldGeoNodeUuid(fieldGeoService.highestPostGeoNode(principal.getPostFieldGeoNodeUuidMap()).getUuid())
                .createdAt(System.currentTimeMillis())
                .modifiedAt(System.currentTimeMillis())
                .status(ODApplicationStatus.OPEN)
                .build();
        odApplicationRepository.save(odApplication);
        return odApplicationTransformer.transform(odApplication);
    }

    private String generateReceiptNumber(Map<Post, List<UUID>> postFieldGeoNodeUuidMap) {
      FieldGeoNode fieldGeoNode=  fieldGeoService.highestPostGeoNode(postFieldGeoNodeUuidMap);
      String jurisdictionName = fieldGeoNode.getName().replace("  "," ")
              .replace(" ","_");
        // Get the current date
        LocalDate currentDate = LocalDate.now();
        // Define the date formatter with the desired pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
        // Format the current date
        String formattedDate = currentDate.format(formatter);
      return String.format("%s_%s_%s", jurisdictionName, formattedDate, System.currentTimeMillis()/1000);
    }

    public ODApplicationPojo update(UUID uuid, ODApplicationPojo odApplicationPojo) {
//        OdApplicationValidatorService.validate(odApplicationPojo);
        OdApplication odApplication = odApplicationRepository.findByUuid(uuid);
        if(!Objects.isNull(odApplicationPojo.getEnquiryOfficerUuid())) {
            User enquiryOfficer = userRepository.findByUuid(odApplicationPojo.getFieldGeoNodeUuid());
            if(enquiryOfficer == null) {
                throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_UUID));
            }
            FieldGeoNode fieldGeoNode = fieldGeoService.resolveFieldGeoNode(enquiryOfficer.getPostFieldGeoNodeUuidMap());
            odApplication.setEnquiryOfficerUuid(enquiryOfficer.getUuid());
            odApplication.setFieldGeoNodeUuid(fieldGeoNode.getUuid());
            odApplication.setStatus(ODApplicationStatus.ENQUIRY);
        }
        if(StringUtils.isNotEmpty(odApplicationPojo.getEnquiryFilePath())) {
            odApplication.setEnquiryFilePath(odApplicationPojo.getEnquiryFilePath());
            odApplication.setStatus(ODApplicationStatus.REVIEW);
        }
        if(ODApplicationStatus.CLOSED.equals(odApplicationPojo.getStatus())) {
            odApplication.setStatus(ODApplicationStatus.CLOSED);
        }

        odApplication.setModifiedAt(System.currentTimeMillis());
        odApplicationRepository.save(odApplication);
        return null;
    }

    public ODApplicationPojo get(UUID odUuid, User principal) {
        OdApplication odApplication = odApplicationRepository.findByUuid(odUuid);
        return odApplicationTransformer.transform(odApplication);
    }

    public List<ODApplicationPojo> getList(String odApplicationStatus, User principal) {
        ODApplicationStatus status = null;
        if(StringUtils.isNotEmpty(odApplicationStatus)) {
            status = ODApplicationStatus.valueOf(odApplicationStatus);
        }
        List<FieldGeoNode> fieldNodes = fieldGeoService.getOwnershipGeoNode(principal.getPostFieldGeoNodeUuidMap());
        if(CollectionUtils.isEmpty(fieldNodes)){
            return  odApplicationRepository.findByOdUuidAndStatus(principal.getUuid(), status)
                    .stream()
                    .map(odApplicationTransformer::transform)
                    .collect(Collectors.toList());
        }
        return  odApplicationRepository.findByFieldGeoNodeUuidInAndStatus(fieldGeoService.getAllChildren(principal.getPostFieldGeoNodeUuidMap()), status)
                .stream()
                .map(odApplicationTransformer::transform)
                .collect(Collectors.toList());

    }

    public List<ODApplicationPojo> getReceiptList(User principal) {
        List<FieldGeoNode> fieldNodes = fieldGeoService.getOwnershipGeoNode(principal.getPostFieldGeoNodeUuidMap());
        if(CollectionUtils.isEmpty(fieldNodes)){
            return  odApplicationRepository.findByOdUuid(principal.getUuid())
                    .stream()
                    .map(odApplicationTransformer::transform)
                    .collect(Collectors.toList());
        }
        return  odApplicationRepository.findByFieldGeoNodeUuidIn(fieldGeoService.getAllChildren(principal.getPostFieldGeoNodeUuidMap()))
                .stream()
                .map(odApplicationTransformer::transform)
                .collect(Collectors.toList());

    }
}
