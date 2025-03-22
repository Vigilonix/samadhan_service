package com.vigilonix.samadhan.transformer;

import com.vigilonix.samadhan.enums.GeoHierarchyType;
import com.vigilonix.samadhan.enums.NotificationMethod;
import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.pojo.FirestoreNotificationRequest;
import com.vigilonix.samadhan.pojo.NotificationPayload;
import com.vigilonix.samadhan.repository.UserRepositoryCustom;
import com.vigilonix.samadhan.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationFirestoreStateChangeSSETransformer implements Transformer<OdApplication, NotificationPayload> {
    private final GeoHierarchyService geoHierarchyService;
    private final UserRepositoryCustom userRepositoryCustom;
    @Override
    public NotificationPayload transform(OdApplication odApplication) {
        UUID ownerGeoHierarachyNode =
                GeoHierarchyType.BEAT.equals(geoHierarchyService.getNodeById(odApplication.getGeoHierarchyNodeUuid()).getType())
                        ?                geoHierarchyService.getParentMap().get(odApplication.getGeoHierarchyNodeUuid()).getUuid()
                        : odApplication.getGeoHierarchyNodeUuid();

        User authorityUser = userRepositoryCustom.findAuthorityGeoHierarchyUser(ownerGeoHierarachyNode);
        if(authorityUser==null) {
            throw new IllegalArgumentException("no one is owner of this geofence"+ odApplication.getGeoHierarchyNodeUuid());
        }
        Map<String,Object> dataMap = Map.of("last_od_application_refresh_epoch", System.currentTimeMillis());
        return         NotificationPayload.builder()
                .notificationMethod(NotificationMethod.SSE_EVENT)
                .request(FirestoreNotificationRequest.builder()
                        .to(authorityUser.getUuid().toString())
                        .dataMap(dataMap)
                        .build())
                .build();
    }
}