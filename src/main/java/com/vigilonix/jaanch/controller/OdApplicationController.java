package com.vigilonix.jaanch.controller;

import com.vigilonix.jaanch.aop.LogPayload;
import com.vigilonix.jaanch.helper.AuthHelper;
import com.vigilonix.jaanch.pojo.AnalyticalResponse;
import com.vigilonix.jaanch.pojo.OdApplicationPayload;
import com.vigilonix.jaanch.service.OdApplicationService;
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
    public OdApplicationPayload create(@RequestBody OdApplicationPayload publicApplicationPojo) {
        return odApplicationService.create(publicApplicationPojo, authHelper.getPrincipal());
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
    public List<OdApplicationPayload> getOdApplication(@RequestParam(name = "status", required = false) String status) {
        return odApplicationService.getList(status, authHelper.getPrincipal());
    }

    @LogPayload
    @GetMapping(path = "/receipt", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<OdApplicationPayload> getOdApplication() {
        return odApplicationService.getReceiptList(authHelper.getPrincipal());
    }

    @GetMapping(path = "/analytics", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public AnalyticalResponse getDashboardAnalytics() {
        return odApplicationService.getDashboardAnalytics(authHelper.getPrincipal());
    }
}
