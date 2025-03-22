package com.vigilonix.samadhan.transformer;

import com.vigilonix.samadhan.config.Constant;
import com.vigilonix.samadhan.enums.GeoHierarchyType;
import com.vigilonix.samadhan.enums.NotificationMethod;
import com.vigilonix.samadhan.enums.NotificationType;
import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.pojo.FirebaseCloudMessageRequest;
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
public class ApplicationPendingGeoFenceOwnerFirebaseCloudMessageTransformer implements Transformer<OdApplication, NotificationPayload> {
    private final GeoHierarchyService geoHierarchyService;
    private final UserRepositoryCustom userRepositoryCustom;
    @Override
    public NotificationPayload transform(OdApplication odApplication) {
        UUID ownerGeoHierarachyNode =
                GeoHierarchyType.BEAT.equals(geoHierarchyService.getNodeById(odApplication.getGeoHierarchyNodeUuid()).getType())
                        ?                geoHierarchyService.getParentMap().get(odApplication.getGeoHierarchyNodeUuid()).getUuid()
                        : odApplication.getGeoHierarchyNodeUuid();

        User authorityUser = userRepositoryCustom.findAuthorityGeoHierarchyUser(ownerGeoHierarachyNode);
        if(authorityUser == null) {
            log.error("failed to find owner user for geofence {} for odApplication {}", ownerGeoHierarachyNode, odApplication);
            throw new IllegalArgumentException("no one is owner of this geofence"+ odApplication.getGeoHierarchyNodeUuid());
        }
        log.info("authority user for geonode {} is {}", ownerGeoHierarachyNode, authorityUser);
        Map<String, String> dataMap = Map.of(
        Constant.CLICK_ACTION, Constant.FLUTTER_NOTIFICATION_CLICK ,
                Constant.TYPE, NotificationType.OD_APPLICATION_CREATED.name());
        return         NotificationPayload.builder()
                .notificationMethod(NotificationMethod.NOTIFICATION_CLOUD_MESSAGE)
                .request(FirebaseCloudMessageRequest.builder()
                        .to(authorityUser.getDeviceToken())
                        .title("Jaanch")
                        .body(String.format("Report %s created", odApplication.getReceiptNo()))
                        .data(dataMap)
                        .build())
                .build();
    }
}