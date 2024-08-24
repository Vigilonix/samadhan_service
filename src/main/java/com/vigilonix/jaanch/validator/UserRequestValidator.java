package com.vigilonix.jaanch.validator;

import com.vigilonix.jaanch.enums.ValidationError;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
import com.vigilonix.jaanch.request.UserRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserRequestValidator implements Validator<List<ValidationError>, UserRequest> {
    private final int MAX_JOB_LENGTH = 130;
    private final int MAX_EDUCATION_LENGTH = 250;
    @Override
    public List<ValidationError> validate(UserRequest user) {
        List<ValidationError> errors = new ArrayList<>();
        if (StringUtils.isNotEmpty(user.getEmail()) && !EmailValidator.getInstance().isValid(user.getEmail())) {
            errors.add(ValidationErrorEnum.INVALID_EMAIL_FORMAT);
        }

//        if (StringUtils.isEmpty(user.getUsername()) || user.getUsername().length() > 50) {
//            errors.add(ValidationErrorEnum.INVALID_EMAIL_FORMAT);
//        }
        if (user.getName() != null && (user.getName().length() > 64 || StringUtils.countMatches(user.getName(), '\n') > 0)) {
            errors.add(ValidationErrorEnum.NAME_ATTRIBUTE_LENGTH_MORE_THAN_EXPECTED);
        }
        return errors;
    }

}
