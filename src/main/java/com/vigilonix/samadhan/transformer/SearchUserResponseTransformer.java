package com.vigilonix.samadhan.transformer;


import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.request.UserResponse;
import com.vigilonix.samadhan.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

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
                .postGeoHierarchyNodeUuidMap(principal.getPostGeoHierarchyNodeUuidMap().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(uuid -> geoHierarchyService.cloneWithBeatChildren(geoHierarchyService.getNodeById(uuid)))
                                .collect(Collectors.toList())
                )))
                .build();
    }
}
