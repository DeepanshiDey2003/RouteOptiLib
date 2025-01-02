package com.example.routeoptilib.services;

import com.moveinsync.vehiclemanagementservice.models.VehicleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {
    
    @Autowired
    private ExternalApiService externalApiService;
    
    public String optimiseRoute(String buid, long startTimestamp, long endTimestamp) {
        List<VehicleDTO> vehicleDTOList = externalApiService.getVehiclesByBuid(buid);
        return "Optimised Route";
    }
}
