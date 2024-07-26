package com.vigilonix.jaanch.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class AuthController {
    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }
}
