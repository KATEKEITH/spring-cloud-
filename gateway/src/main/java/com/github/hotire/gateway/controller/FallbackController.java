package com.github.hotire.gateway.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @RequestMapping(value = "/fallback", produces = MediaType.APPLICATION_JSON_VALUE)
    public String fallback() {
        return "{\"message\":\"temporarily unavailable, please retry.\"}";
    }
    
}
