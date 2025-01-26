package com.vigilonix.applicationnadministrativeservice.validator;

import com.vigilonix.applicationnadministrativeservice.enums.ValidationError;
import com.vigilonix.applicationnadministrativeservice.enums.ValidationErrorEnum;
import com.vigilonix.applicationnadministrativeservice.pojo.ODApplicationValidationPayload;
import com.vigilonix.applicationnadministrativeservice.pojo.OdApplicationPayload;
import com.vigilonix.applicationnadministrativeservice.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OdApplicationCreationValidator implements Validator<List<ValidationError>, ODApplicationValidationPayload> {
    private final GeoHierarchyService geoHierarchyService;
    private final PDFValidator pdfValidator;

    @Override
    public List<ValidationError> validate(ODApplicationValidationPayload odApplicationValidationPayload) {
        List<ValidationError> errors = new ArrayList<>();
        OdApplicationPayload odRequest = odApplicationValidationPayload.getOdApplicationPayload();
        if (!Objects.isNull(odRequest.getGeoHierarchyNodeUuid()) && Objects.isNull(geoHierarchyService.getNodeById(odRequest.getGeoHierarchyNodeUuid()))) {
            errors.add(ValidationErrorEnum.INVALID_GEONODE_UUID);
        }

        if (!Objects.isNull(odRequest.getGeoHierarchyNodeUuid()) && !Objects.isNull(geoHierarchyService.getNodeById(odRequest.getGeoHierarchyNodeUuid())) &&
                !geoHierarchyService.getAllLevelNodes(odApplicationValidationPayload.getPrincipalUser().getPostGeoHierarchyNodeUuidMap())
                        .contains(odRequest.getGeoHierarchyNodeUuid())) {
            errors.add(ValidationErrorEnum.INVALID_GRANT);
        }
        if (StringUtils.isEmpty(odRequest.getApplicantName()) || odRequest.getApplicantName().length() > 64) {
            errors.add(ValidationErrorEnum.NAME_ATTRIBUTE_LENGTH_MORE_THAN_EXPECTED);
        }
        if (StringUtils.isEmpty(odRequest.getApplicantPhoneNumber()) || odRequest.getApplicantPhoneNumber().length() != 10 || !odRequest.getApplicantPhoneNumber().chars().allMatch(Character::isDigit)) {
            errors.add(ValidationErrorEnum.INVALID_PHONE_NUMBER);
        }
        List<UUID> associatedGeoHierarchyNodeSet = geoHierarchyService.getAllLevelNodes(
                odApplicationValidationPayload.getPrincipalUser().getPostGeoHierarchyNodeUuidMap()
        );


        if (CollectionUtils.isNotEmpty(odApplicationValidationPayload.getGeoHierarchyNodeUuids()) && !CollectionUtils.isSubCollection(
                odApplicationValidationPayload.getGeoHierarchyNodeUuids(),
                associatedGeoHierarchyNodeSet)) {
            errors.add(ValidationErrorEnum.INVALID_GRANT);
        }
        errors.addAll(pdfValidator.validate(odRequest.getApplicationFilePath()));
        return errors;
    }

}
