package com.vigilonix.samadhan.controller;

import com.vigilonix.samadhan.helper.AuthHelper;
import com.vigilonix.samadhan.pojo.GeoHierarchyNode;
import com.vigilonix.samadhan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/geo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeoController {
    private final AuthHelper authHelper;
    private final UserService userService;

    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<GeoHierarchyNode> searchGeoFences(@RequestParam(value = "prefix_name", defaultValue = "")String prefixName) {
        return userService.searchGeoFence(authHelper.getPrincipal(), prefixName);
    }

}
