package com.vigilonix.jaanch.controller;

import com.vigilonix.jaanch.aop.LogPayload;
import com.vigilonix.jaanch.helper.AuthHelper;
import com.vigilonix.jaanch.pojo.ChartData;
import com.vigilonix.jaanch.pojo.GroupData;
import com.vigilonix.jaanch.pojo.KandFilter;
import com.vigilonix.jaanch.pojo.KandPayload;
import com.vigilonix.jaanch.service.KandService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/kand")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KaandController {
    public static final String UUID = "/{uuid}";
    private final KandService kandService;
    private final AuthHelper authHelper;

    @LogPayload
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public KandPayload create(@RequestBody KandPayload kandPayload, @RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return kandService.createKand(kandPayload, authHelper.getPrincipal(), geoHierarchyNodeUuids);
    }

    @LogPayload
    @PutMapping(path = UUID, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public KandPayload update(@PathVariable(name = "uuid") java.util.UUID kandUuid, @RequestBody KandPayload kandPayload) {
        return kandService.updateKand(kandUuid, kandPayload, authHelper.getPrincipal());
    }

    @LogPayload
    @GetMapping(path = UUID, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public KandPayload get(@PathVariable(name = "uuid") java.util.UUID kandUuid) {
        return kandService.getKand(kandUuid, authHelper.getPrincipal());
    }

    @LogPayload
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<KandPayload> getList(@RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return kandService.getKandList(authHelper.getPrincipal(), geoHierarchyNodeUuids);
    }

    @LogPayload
    @PostMapping(path = "/filter", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<KandPayload> filter(@RequestBody KandFilter kandFilter, @RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return kandService.getKandFilterList(authHelper.getPrincipal(), geoHierarchyNodeUuids, kandFilter);
    }

    @LogPayload
    @PostMapping(path = "/chart/week_trend", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChartData getKandWeekTrend(@RequestBody KandFilter kandFilter, @RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return kandService.getKandWeekDayTrend(authHelper.getPrincipal(), geoHierarchyNodeUuids, kandFilter);
    }

    @LogPayload
    @PostMapping(path = "/chart/hour_trend", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChartData getKandHourTrend(@RequestBody KandFilter kandFilter, @RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return kandService.getKandHourTrend(authHelper.getPrincipal(), geoHierarchyNodeUuids, kandFilter);
    }

    @LogPayload
    @PostMapping(path = "/chart/tag_counter", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupData> getTagCounters(@RequestBody KandFilter kandFilter, @RequestParam(name = "geo_hierarchy_node_uuids", required = false)List<java.util.UUID> geoHierarchyNodeUuids) {
        return kandService.getTagCounters(authHelper.getPrincipal(), geoHierarchyNodeUuids, kandFilter);
    }


}
