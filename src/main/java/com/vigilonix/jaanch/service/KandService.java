package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.KandPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Transactional
public class KandService {
    public KandPayload createKand(KandPayload kandPayload, User principal, List<UUID> geoHierarchyNodeUuids) {
        return null;
    }

    public KandPayload updateKand(UUID kandUuid, KandPayload kandPayload, User principal) {
        return null;
    }

    public KandPayload getKand(UUID kandUuid, User principal) {
        return null;
    }

    public List<KandPayload> getKandList(User principal, List<UUID> geoHierarchyNodeUuids) {
        return null;
    }
}
