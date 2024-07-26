package com.vigilonix.jaanch.transformer;


import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.request.UserResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class UserResponseTransformer implements Transformer<User, UserResponse> {
    public static final long MILLISECONDS_IN_ONE_YEAR = 365 * 24 * 3600 * 1000L;

    @Override
    public UserResponse transform(User principal) {

        return UserResponse.builder()
                .username(principal.getUsername())
                .email(principal.getEmail())

                .createdOn(principal.getCreatedOn())


                .uuid(principal.getUuid())


                .build();
    }
}
