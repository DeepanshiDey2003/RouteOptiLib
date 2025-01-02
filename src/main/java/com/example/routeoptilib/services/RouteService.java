package com.example.routeoptilib.services;

import com.example.routeoptilib.models.Block;
import com.moveinsync.ets.models.Duty;
import com.moveinsync.ets.models.EmptyLeg;
import com.moveinsync.tripsheet.models.TripsheetDTOV2;
import com.moveinsync.vehiclemanagementservice.models.VehicleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class RouteService {
    
    @Autowired
    private ExternalApiService externalApiService;
    
    public String optimiseRoute(String buid, long startTimestamp, long endTimestamp) {
        List<Block> cabBlocks = new ArrayList<>();
        List<Block> driverBlocks = new ArrayList<>();
        
        List<VehicleDTO> vehicleDTOList = externalApiService.getVehiclesByBuid(buid);
        List<String> vehicleRegistrations = vehicleDTOList.stream().map(VehicleDTO::getRegistration).toList();
        
        List<TripsheetDTOV2> vehicleTripDetailsOfAllVehiclesForDay = new ArrayList<>();
        for (String vehicleRegistration : vehicleRegistrations) {
            vehicleTripDetailsOfAllVehiclesForDay.addAll(externalApiService.getVehicleTripsForTheDay(vehicleRegistration, getStartTimestampOfDay(), getEndTimestampOfDay()));
        }
        
        // Create blocks for each cab and driver when both are occupied (tripsheet exists)
        for (TripsheetDTOV2 tripsheetDTOV2 : vehicleTripDetailsOfAllVehiclesForDay) {
            Block cabBlock = new Block(tripsheetDTOV2.getCab().getRegistration(), tripsheetDTOV2.getStartTime().getTime(), tripsheetDTOV2.getEndTime().getTime());
            Block driverBlock = new Block(tripsheetDTOV2.getCab().getDriver().getGuid(), tripsheetDTOV2.getStartTime().getTime(), tripsheetDTOV2.getEndTime().getTime());
            cabBlocks.add(cabBlock);
            driverBlocks.add(driverBlock);
        }
        
        // Create blocks for each cab and driver when both are occupied (emptyLeg exists)
        for (TripsheetDTOV2 tripsheetDTOV2 : vehicleTripDetailsOfAllVehiclesForDay) {
            Duty duty = externalApiService.getDutyForTrip(buid, tripsheetDTOV2.getGuid());
            for (EmptyLeg emptyLeg : duty.getEmptyLegs()) {
                Block emptyLegCabBlock = new Block(tripsheetDTOV2.getCab().getRegistration(), emptyLeg.getStartTime(), emptyLeg.getEndTime());
                Block emptyLegDriverBlock = new Block(tripsheetDTOV2.getCab().getDriver().getGuid(), emptyLeg.getStartTime(), emptyLeg.getEndTime());
                cabBlocks.add(emptyLegCabBlock);
                driverBlocks.add(emptyLegDriverBlock);
            }
        }
        
        return "Optimised Route";
    }
    
    private long getStartTimestampOfDay() {
        // Set to start of the day (00:00:00)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    private long getEndTimestampOfDay() {
        // Set to end of the day (23:59:59)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }
}
