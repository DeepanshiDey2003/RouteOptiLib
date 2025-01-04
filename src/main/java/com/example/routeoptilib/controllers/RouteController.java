package com.example.routeoptilib.controllers;

import com.example.routeoptilib.models.Cabby;
import com.example.routeoptilib.models.EventDataDTO;
import com.example.routeoptilib.models.OptimisedSuggestionDataDTO;
import com.example.routeoptilib.models.RouteDetailDTO;
import com.example.routeoptilib.services.RouteService;
import com.mis.serverdata.utils.GsonUtils;
import com.moveinsync.vehiclemanagementservice.models.VehicleDTO;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RouteController {
    
    @Autowired
    private RouteService routeService;
    
    @GetMapping("/cabs")
    public List<VehicleDTO> getAllCabs(@PathVariable("buid") String buid) {
        return routeService.getAllCabs(buid);
    }
    
    @GetMapping("/drivers")
    public List<Cabby> getAllDrivers(@PathVariable("buid") String buid) {
        return routeService.getAllDrivers(buid);
    }
    
    @PostMapping("/events")
    public void createEvents(@PathVariable("buid") String buid, @RequestBody List<EventDataDTO> events) {
        routeService.createEvents(buid, events);
    }
    
    @GetMapping("/optimise")
    public List<OptimisedSuggestionDataDTO> optimiseRoute(@PathVariable("buid") String buid, @RequestParam("routeId") String routeId, @RequestParam("startTime") long startTimestamp, @RequestParam("endTime") long endTimestamp) {
        return routeService.provideOptimisedSuggestion(buid, routeId, startTimestamp, endTimestamp);
    }
    
    @GetMapping("/details")
    public String getShuttleRoutes(@PathVariable("buid") String buid) {
        List<RouteDetailDTO> r = routeService.getShuttleRoutes(buid);
        return GsonUtils.getGson().toJson(r);
    }
}
