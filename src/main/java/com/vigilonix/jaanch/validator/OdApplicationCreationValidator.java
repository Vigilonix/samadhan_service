package com.vigilonix.jaanch.validator;

import com.vigilonix.jaanch.enums.ValidationError;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
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
public class OdApplicationCreationValidator implements Validator<List<ValidationError>, ODApplicationValidationPayload> {
    private final FieldGeoService fieldGeoService;
    private final PDFValidator pdfValidator;
    @Override
    public List<ValidationError> validate(ODApplicationValidationPayload odApplicationValidationPayload) {
        List<ValidationError> errors = new ArrayList<>();
        OdApplicationPayload odRequest = odApplicationValidationPayload.getOdApplicationPayload();
        if(Objects.isNull(odRequest.getFieldGeoNodeUuid()) || Objects.isNull(fieldGeoService.getFieldGeoNode(odRequest.getFieldGeoNodeUuid()))) {
            errors.add(ValidationErrorEnum.INVALID_GEONODE_UUID);
        }
        if(StringUtils.isEmpty(odRequest.getApplicantName()) || odRequest.getApplicantName().length()>64) {
            errors.add(ValidationErrorEnum.NAME_ATTRIBUTE_LENGTH_MORE_THAN_EXPECTED);
        }
        if(StringUtils.isEmpty(odRequest.getApplicantPhoneNumber()) || odRequest.getApplicantPhoneNumber().length()!=10 || odRequest.getApplicantPhoneNumber().chars().allMatch(Character::isDigit)) {
            errors.add(ValidationErrorEnum.INVALID_PHONE_NUMBER);
        }
        errors.addAll(pdfValidator.validate(odRequest.getApplicationFilePath()));
        return errors;
    }

}
