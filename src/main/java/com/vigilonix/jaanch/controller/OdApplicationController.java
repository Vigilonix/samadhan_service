package com.vigilonix.jaanch.controller;

import com.vigilonix.jaanch.aop.LogPayload;
import com.vigilonix.jaanch.helper.AuthHelper;
import com.vigilonix.jaanch.pojo.ODApplicationPayload;
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

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ODApplicationPayload create(@RequestBody ODApplicationPayload publicApplicationPojo) {
        return odApplicationService.create(publicApplicationPojo, authHelper.getPrincipal());
    }

    @LogPayload
    @PutMapping(path = UUID, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ODApplicationPayload update(@PathVariable(name = "uuid") String odApplicationUuid, @RequestBody ODApplicationPayload odApplicationPayload) {
        return odApplicationService.update(java.util.UUID.fromString(odApplicationUuid), odApplicationPayload, authHelper.getPrincipal());
    }

    @GetMapping(path = UUID, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ODApplicationPayload getOdApplicationByUuid(@PathVariable(name = "uuid") String odApplicationUuid) {
        return odApplicationService.get(java.util.UUID.fromString(odApplicationUuid), authHelper.getPrincipal());
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<ODApplicationPayload> getOdApplication(@RequestParam(name = "status", required = false) String status) {
        return odApplicationService.getList(status, authHelper.getPrincipal());
    }
    @GetMapping(path = "/receipt", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<ODApplicationPayload> getOdApplication() {
        return odApplicationService.getReceiptList(authHelper.getPrincipal());
    }
}
