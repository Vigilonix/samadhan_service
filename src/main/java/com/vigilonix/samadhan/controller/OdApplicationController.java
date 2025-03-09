package com.vigilonix.samadhan.controller;

import com.vigilonix.samadhan.aop.LogPayload;
import com.vigilonix.samadhan.helper.AuthHelper;
import com.vigilonix.samadhan.pojo.AnalyticalResponse;
import com.vigilonix.samadhan.pojo.OdApplicationPayload;
import com.vigilonix.samadhan.pojo.OdAssignmentPayload;
import com.vigilonix.samadhan.service.OdApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/od_application")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OdApplicationController {
    public static final String UUID = "/{uuid}";
    private final OdApplicationService odApplicationService;
    private final AuthHelper authHelper;

    @LogPayload
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OdApplicationPayload create(@RequestBody OdApplicationPayload publicApplicationPojo, @RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return odApplicationService.create(publicApplicationPojo, authHelper.getPrincipal(), geoHierarchyNodeUuids);
    }

    @LogPayload
    @PostMapping(value = "/{uuid}/assignment", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createAssignment(@PathVariable(name = "uuid") String odApplicationUuid, @RequestBody List<OdAssignmentPayload> assignments, @RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        odApplicationService.createAssignment(assignments, java.util.UUID.fromString(odApplicationUuid), authHelper.getPrincipal(), geoHierarchyNodeUuids);
    }

    @LogPayload
    @PutMapping(value = "/assignment/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateAssignment(@PathVariable(name = "uuid") String odApplicationUuid, @RequestBody List<OdAssignmentPayload> assignments, @RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        odApplicationService.createAssignment(assignments, java.util.UUID.fromString(odApplicationUuid), authHelper.getPrincipal(), geoHierarchyNodeUuids);
    }

    @LogPayload
    @PutMapping(path = UUID, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OdApplicationPayload update(@PathVariable(name = "uuid") String odApplicationUuid, @RequestBody OdApplicationPayload odApplicationPayload) {
        return odApplicationService.update(java.util.UUID.fromString(odApplicationUuid), odApplicationPayload, authHelper.getPrincipal());
    }

    @LogPayload
    @GetMapping(path = UUID, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OdApplicationPayload getOdApplicationByUuid(@PathVariable(name = "uuid") String odApplicationUuid) {
        return odApplicationService.get(java.util.UUID.fromString(odApplicationUuid), authHelper.getPrincipal());
    }

    @LogPayload
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<OdApplicationPayload> getOdApplication(@RequestParam(name = "status", required = false) String status, @RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return odApplicationService.getList(status, authHelper.getPrincipal(), geoHierarchyNodeUuids);
    }

    @LogPayload
    @GetMapping(path = "/receipt", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<OdApplicationPayload> getOdApplication(@RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return odApplicationService.getReceiptList(authHelper.getPrincipal(), geoHierarchyNodeUuids);
    }

    @GetMapping(path = "/analytics", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public AnalyticalResponse getDashboardAnalytics(@RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return odApplicationService.getDashboardAnalytics(authHelper.getPrincipal(), geoHierarchyNodeUuids);
    }

    @GetMapping(path = "/analytic", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String getDashboardAnalytic(@RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return odApplicationService.getAnalytics(authHelper.getPrincipal(), geoHierarchyNodeUuids);
    }
}
