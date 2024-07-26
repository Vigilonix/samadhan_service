package com.vigilonix.jaanch.validator;

import com.vigilonix.jaanch.enums.ValidationError;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
import com.vigilonix.jaanch.request.AuthRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClientValidator implements Validator<List<ValidationError>, AuthRequest> {
    public static final String CLIENT_SECRET = "HARI_OM_HARI_OM";
    private static final List<String> CLIENT_IDS = Arrays.asList("web_client", "app");

    @Override
    public List<ValidationError> validate(AuthRequest authRequest) {
        List<ValidationError> errors = new ArrayList<>();
        if (StringUtils.isEmpty(authRequest.getClientId()) || StringUtils.isEmpty(authRequest.getClientSecret())) {
            errors.add(ValidationErrorEnum.UNAUTHORIZED_REQUEST);
        } else if (!CLIENT_SECRET.equals(authRequest.getClientSecret())) {
            errors.add(ValidationErrorEnum.UNAUTHORIZED_REQUEST);
        } else if (!CLIENT_IDS.contains(authRequest.getClientId())) {
            errors.add(ValidationErrorEnum.UNAUTHORIZED_REQUEST);
        }
        return errors;
    }
}
