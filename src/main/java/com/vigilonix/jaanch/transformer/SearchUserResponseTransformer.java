package com.vigilonix.jaanch.transformer;


import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.request.UserResponse;
import com.vigilonix.jaanch.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class SearchUserResponseTransformer implements Transformer<User, UserResponse> {
    public static final long MILLISECONDS_IN_ONE_YEAR = 365 * 24 * 3600 * 1000L;
    private final GeoHierarchyService geoHierarchyService;

    @Override
    public UserResponse transform(User principal) {
        return UserResponse.builder()
                .name(principal.getName())
                .uuid(principal.getUuid())
                .rank(principal.getRank())
                .build();
    }
}
