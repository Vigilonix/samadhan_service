package com.vigilonix.samadhan.validator;

import com.vigilonix.samadhan.enums.ValidationError;
import com.vigilonix.samadhan.enums.ValidationErrorEnum;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.pojo.OdApplicationPayload;
import com.vigilonix.samadhan.pojo.ODApplicationValidationPayload;
import com.vigilonix.samadhan.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OdApplicationUpdateValidator implements Validator<List<ValidationError>, ODApplicationValidationPayload> {
    private final GeoHierarchyService geoHierarchyService;
    private final PDFValidator pdfValidator;
    private final OdApplicationCreationValidator odApplicationCreationValidator;
    @Override
    public List<ValidationError> validate(ODApplicationValidationPayload odApplicationValidationPayload) {
        List<ValidationError> errors = new ArrayList<>();
        OdApplicationPayload odRequest = odApplicationValidationPayload.getOdApplicationPayload();
        User principal = odApplicationValidationPayload.getPrincipalUser();
        if(Objects.isNull(odApplicationValidationPayload.getOdApplication())) {
            return Collections.singletonList(ValidationErrorEnum.INVALID_UUID);
        }
            return odApplicationCreationValidator.validate(odApplicationValidationPayload);
//        if((!Objects.isNull(odApplicationValidationPayload.getEnquiryUser()) ||
//                Sets.newHashSet(OdApplicationStatus.REVIEW)
//                    .contains(odApplicationValidationPayload.getOdApplication().getStatus()))
//                && !geoHierarchyService.hasAuthority(odApplicationValidationPayload.getOdApplication().getGeoHierarchyNodeUuid(),principal.getPostGeoHierarchyNodeUuidMap())) {
//            errors.add(ValidationErrorEnum.INVALID_GRANT);
//        }
//
//        if(CollectionUtils.isNotEmpty(odRequest.getEnquiries()) && StringUtils.isNotEmpty(odRequest.getEnquiries().get(0).getPath())) {
//            odRequest.getEnquiries().forEach(e-> {
//                errors.addAll(pdfValidator.validate(e.getPath()));
//                if (!principal.getUuid().equals(e.getOwnerUuid())) {
//                    errors.add(ValidationErrorEnum.INVALID_GRANT);
//                }
//            });
//        }
//        return errors;
    }

}
