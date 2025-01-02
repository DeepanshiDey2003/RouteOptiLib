package com.example.routeoptilib.controllers;

import com.example.routeoptilib.services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( "/route")
public class RouteController {
    
    @Autowired
    private RouteService routeService;
    
    @GetMapping("/optimise")
    public String optimiseRoute() {
        return routeService.optimiseRoute();
    }
}
