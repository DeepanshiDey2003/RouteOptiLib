package com.example.routeoptilib.controllers;

import com.example.routeoptilib.models.CabDTO;
import com.example.routeoptilib.models.BlockDataDTO;
import com.example.routeoptilib.models.DriverDTO;
import com.example.routeoptilib.models.OptimisedSuggestionDataDTO;
import com.example.routeoptilib.models.RouteDetailDTO;
import com.example.routeoptilib.persistence.entity.CabDriverRouteMapping;
import com.example.routeoptilib.persistence.repository.CabDriverRouteMappingRepository;
import com.example.routeoptilib.persistence.repository.CabRepository;
import com.example.routeoptilib.persistence.repository.DriverRepository;
import com.example.routeoptilib.persistence.repository.RouteRepository;
import com.example.routeoptilib.services.RouteService;
import com.example.routeoptilib.services.ScheduleService;
import com.example.routeoptilib.utils.ConversionUtil;
import com.example.routeoptilib.utils.LoadedBuidsChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping( "/route/{buid}")
@CrossOrigin
@RequiredArgsConstructor
public class RouteController {
    
    private final RouteService routeService;
    private final DriverRepository driverRepository;
    private final CabRepository cabRepository;
    private final RouteRepository routeRepository;
    private final CabDriverRouteMappingRepository cabDriverRouteMappingRepository;
  
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
    
    @DeleteMapping("/reset-all")
    public void resetAllEntities(@PathVariable("buid") String buid) {
        driverRepository.deleteAllByBuid(buid);
        cabRepository.deleteAllByBuid(buid);
        routeRepository.deleteAllByBuid(buid);
        cabDriverRouteMappingRepository.deleteAllByBuid(buid);
        LoadedBuidsChecker.removeAllForBuid(buid);
    }
    
    @PostMapping("/generate/multi")
    public boolean generateRoutes(@PathVariable("buid") String buid, @RequestBody List<CabDriverRouteMapping> mappings) {
      return routeService.createMapping(buid, mappings);
    }
    
    @GetMapping("/properties")
  public Map<String, Integer> getProperties(@PathVariable("buid") String buid) {
      Map<String, Integer> properties = new HashMap<>();
      for (ScheduleService.GlobalProperties global : ScheduleService.GlobalProperties.values()) {
        if (ScheduleService.GlobalProperties.LUNCH_BREAK_START_TIME_MINUTES_FROM_MIDNIGHT.equals(global)
                || ScheduleService.GlobalProperties.LUNCH_BREAK_END_TIME_MINUTES_FROM_MIDNIGHT.equals(global)) {
          properties.put(global.name(), ConversionUtil.getFormattedTime(global.getNumValue()));
          continue;
        }
        properties.put(global.name(), global.getNumValue());
      }
      return properties;
    }
    
    @PostMapping("/properties")
    public void updateProperties(@PathVariable("buid") String buid, @RequestBody Map<String, Integer> properties) {
      for (ScheduleService.GlobalProperties global : ScheduleService.GlobalProperties.values()) {
        var value = properties.get(global.name());
        if (value != null) {
          if (ScheduleService.GlobalProperties.LUNCH_BREAK_START_TIME_MINUTES_FROM_MIDNIGHT.equals(global)
                  || ScheduleService.GlobalProperties.LUNCH_BREAK_END_TIME_MINUTES_FROM_MIDNIGHT.equals(global)) {
            global.setValue(ConversionUtil.getHoursMinsFromFormattedTime(value));
            continue;
          }
          global.setValue(value);
        }
      }
    }
}
