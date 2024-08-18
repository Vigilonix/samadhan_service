package com.vigilonix.jaanch.validator;

import com.vigilonix.jaanch.enums.ValidationError;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.ODApplicationStatus;
import com.vigilonix.jaanch.pojo.OdApplicationPayload;
import com.vigilonix.jaanch.pojo.ODApplicationValidationPayload;
import com.vigilonix.jaanch.service.FieldGeoService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OdApplicationUpdateValidator implements Validator<List<ValidationError>, ODApplicationValidationPayload> {
    private final FieldGeoService fieldGeoService;
    private final PDFValidator pdfValidator;
    private final OdApplicationCreationValidator odApplicationCreationValidator;
    @Override
    public List<ValidationError> validate(ODApplicationValidationPayload odApplicationValidationPayload) {
        List<ValidationError> errors = new ArrayList<>();
        OdApplicationPayload odRequest = odApplicationValidationPayload.getOdApplicationPayload();
        User principal = odApplicationValidationPayload.getPrincipalUser();
        if(Objects.isNull(odRequest.getEnquiryOfficerUuid()) && ODApplicationStatus.OPEN.equals(odApplicationValidationPayload.getOdApplication().getStatus())) {
            return odApplicationCreationValidator.validate(odApplicationValidationPayload);
        }
        else if(ODApplicationStatus.OPEN.equals(odApplicationValidationPayload.getOdApplication().getStatus())) {
            if(Objects.isNull(odRequest.getEnquiryOfficerUuid()) || Objects.isNull(odApplicationValidationPayload.getEnquiryUser())) {
                errors.add(ValidationErrorEnum.INVALID_ID);
            }
        }
        if(StringUtils.isNotEmpty(odRequest.getEnquiryFilePath())) {
            errors.addAll(pdfValidator.validate(odRequest.getApplicationFilePath()));
            if(!principal.getUuid().equals(odApplicationValidationPayload.getOdApplication().getEnquiryOfficer().getUuid())) {
                errors.add(ValidationErrorEnum.INVALID_GRANT);
            }
        }
        return errors;
    }

}
