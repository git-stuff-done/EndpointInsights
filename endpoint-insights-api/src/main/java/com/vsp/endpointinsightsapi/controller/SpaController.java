package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.authentication.PublicAPI;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {
    @PublicAPI
    @RequestMapping(value = {"/{path:[^.]*}", "/{path1:[^.]*}/{path2:[^.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
