package com.example.routeoptilib.controllers;

import com.example.routeoptilib.models.CabDTO;
import com.example.routeoptilib.models.BlockDataDTO;
import com.example.routeoptilib.models.DriverDTO;
import com.example.routeoptilib.models.OptimisedSuggestionDataDTO;
import com.example.routeoptilib.models.RouteDetailDTO;
import com.example.routeoptilib.services.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping( "/route/{buid}")
@CrossOrigin
public class RouteController {
    
    @Autowired
    private RouteService routeService;
    
    @GetMapping("/cabs")
    public List<CabDTO> getAllCabs(@PathVariable("buid") String buid) {
        return routeService.getAllCabs(buid);
    }
    
    @GetMapping("/drivers")
    public List<DriverDTO> getAllDrivers(@PathVariable("buid") String buid) {
        return routeService.getAllDrivers(buid);
    }
    
    @PostMapping("/blocks")
    public void createBlocks(@PathVariable("buid") String buid, @RequestBody List<BlockDataDTO> events) {
        routeService.createBlocks(buid, events);
    }
    
    @GetMapping("/suggest")
    public List<OptimisedSuggestionDataDTO> suggestRoute(@PathVariable("buid") String buid) {
        return routeService.suggestCabDriverForRoutes(buid);
    }
    
    @GetMapping("/details")
    public List<RouteDetailDTO> getShuttleRoutes(@PathVariable("buid") String buid) {
        return routeService.getShuttleRoutes(buid);
    }
    
    @PostMapping("/generate/{routeId}")
    public boolean generateRoute(@PathVariable("buid") String buid,
                                 @PathVariable("routeId") String routeId,
                                 @RequestParam(value = "driverLicense") String driverLicense,
                                 @RequestParam(value = "cabIdentification") String cabId) {
        return routeService.createMapping(buid, routeId, driverLicense, cabId);
    }
}
