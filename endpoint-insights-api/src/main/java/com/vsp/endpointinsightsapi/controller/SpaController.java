package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.authentication.PublicAPI;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

@Controller
public class SpaController {
    @PublicAPI
    @RequestMapping(value = {"/{path:[^.]*}", "/{path1:[^.]*}/{path2:[^.]*}"})
    public ResponseEntity<Void> forward(HttpServletRequest request) {
        String path = request.getRequestURI();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://d2wravsw1nwfu2.cloudfront.net" + path))
                .build();
    }
}
