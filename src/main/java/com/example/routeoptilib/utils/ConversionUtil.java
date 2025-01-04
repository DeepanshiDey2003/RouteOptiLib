package com.example.routeoptilib.utils;

import com.example.routeoptilib.models.RouteDetailDTO;
import com.example.routeoptilib.models.StopDetailDTO;
import com.moveinsync.bus.models.dto.ShuttleAvailabilityDTO;
import com.moveinsync.bus.models.dto.ShuttleStopDTO;

import java.util.List;

public class ConversionUtil {
    
    ConversionUtil() {
    }
    
    public static List<RouteDetailDTO> convertToResponseDTO(ShuttleAvailabilityDTO availabilityDTO) {
        return availabilityDTO.getShuttleRoutes().stream().map(route -> {
            List<StopDetailDTO> stopDetails = route.getShuttleStopsTimingMap().entrySet().stream().map(entry -> {
                String stopGuid = entry.getKey();
                Integer plannedArrivalTime = entry.getValue();
                
                ShuttleStopDTO stop = availabilityDTO.getShuttleStops().get(stopGuid);
                return new StopDetailDTO(
                        stop.getGuid(),
                        stop.getLandmark(),
                        stop.getGeoCord(),
                        plannedArrivalTime
                );
            }).toList();
            return new RouteDetailDTO(route.getRouteId(), route.getRouteName(), stopDetails);
        }).toList();
    }
    
}
