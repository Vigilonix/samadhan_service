package com.vigilonix.jaanch.controller;

import com.vigilonix.jaanch.aop.LogPayload;
import com.vigilonix.jaanch.helper.AuthHelper;
import com.vigilonix.jaanch.pojo.ODApplicationPojo;
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
    private final OdApplicationService publicApplicationService;
    private final AuthHelper authHelper;

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ODApplicationPojo create(@RequestBody ODApplicationPojo publicApplicationPojo) {
        return publicApplicationService.create(publicApplicationPojo, authHelper.getPrincipal());
    }

    @LogPayload
    @PutMapping(path = UUID, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ODApplicationPojo update(@PathVariable(name = "uuid") String odApplicationUuid, @RequestBody ODApplicationPojo odApplicationPojo) {
        return publicApplicationService.update(java.util.UUID.fromString(odApplicationUuid), odApplicationPojo);
    }

    @GetMapping(path = UUID, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ODApplicationPojo getOdApplicationByUuid(@PathVariable(name = "uuid") String odApplicationUuid) {
        return publicApplicationService.get(java.util.UUID.fromString(odApplicationUuid), authHelper.getPrincipal());
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<ODApplicationPojo> getOdApplication(@RequestParam(name = "status", required = false) String status) {
        return publicApplicationService.getList(status, authHelper.getPrincipal());
    }
    @GetMapping(path = "/receipt", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<ODApplicationPojo> getOdApplication() {
        return publicApplicationService.getReceiptList(authHelper.getPrincipal());
    }
}
