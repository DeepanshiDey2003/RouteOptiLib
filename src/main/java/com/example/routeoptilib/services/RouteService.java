package com.example.routeoptilib.services;

import com.example.routeoptilib.models.Cabby;
import com.example.routeoptilib.models.RouteDetailDTO;
import com.example.routeoptilib.persistence.entity.Block;
import com.example.routeoptilib.models.EventDataDTO;
import com.example.routeoptilib.models.OptimisedSuggestionDataDTO;
import com.example.routeoptilib.persistence.repository.BlockRepository;
import com.example.routeoptilib.utils.Constant;
import com.example.routeoptilib.utils.ConversionUtil;
import com.moveinsync.bus.models.dto.ShuttleAvailabilityDTO;
import com.moveinsync.ets.models.Duty;
import com.moveinsync.ets.models.EmptyLeg;
import com.moveinsync.tripsheet.models.TripsheetDTOV2;
import com.moveinsync.vehiclemanagementservice.models.VehicleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@Service
public class RouteService {
    
    @Autowired
    private ExternalApiService externalApiService;
    
    @Autowired
    private BlockRepository blockRepository;
    
    public List<OptimisedSuggestionDataDTO> provideOptimisedSuggestion(String buid, String routeId, long startTimestamp, long endTimestamp) {
        
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
            Block cabBlock = new Block(tripsheetDTOV2.getCab().getRegistration(), Constant.CAB, tripsheetDTOV2.getStartTime().getTime(), tripsheetDTOV2.getEndTime().getTime());
            Block driverBlock = new Block(tripsheetDTOV2.getCab().getDriver().getGuid(), Constant.DRIVER, tripsheetDTOV2.getStartTime().getTime(), tripsheetDTOV2.getEndTime().getTime());
            cabBlocks.add(cabBlock);
            driverBlocks.add(driverBlock);
        }
        
        // Create blocks for each cab and driver when both are occupied (emptyLeg exists)
        for (TripsheetDTOV2 tripsheetDTOV2 : vehicleTripDetailsOfAllVehiclesForDay) {
            Duty duty = externalApiService.getDutyForTrip(buid, tripsheetDTOV2.getGuid());
            for (EmptyLeg emptyLeg : duty.getEmptyLegs()) {
                Block emptyLegCabBlock = new Block(tripsheetDTOV2.getCab().getRegistration(), Constant.CAB, emptyLeg.getStartTime(), emptyLeg.getEndTime());
                Block emptyLegDriverBlock = new Block(tripsheetDTOV2.getCab().getDriver().getGuid(), Constant.DRIVER, emptyLeg.getStartTime(), emptyLeg.getEndTime());
                cabBlocks.add(emptyLegCabBlock);
                driverBlocks.add(emptyLegDriverBlock);
            }
        }
        
        cabBlocks.forEach(block -> block.setBuid(buid));
        driverBlocks.forEach(block -> block.setBuid(buid));
        blockRepository.saveAll(cabBlocks);
        blockRepository.saveAll(driverBlocks);
        
        return optimiseRoute(routeId, startTimestamp, endTimestamp);
    }
    
    private List<OptimisedSuggestionDataDTO> optimiseRoute(String routeId, long startTimestamp, long endTimestamp) {
        // To do: Optimise the route based on the blocks
        //Python Logic
        
        List<OptimisedSuggestionDataDTO> optimisedSuggestionDataDTOList = new ArrayList<>();
        return optimisedSuggestionDataDTOList;
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
    
    public List<Cabby> getAllDrivers(String buid) {
        return externalApiService.getDriverData(buid);
    }
    
    public List<VehicleDTO> getAllCabs(String buid) {
        return externalApiService.getVehiclesByBuid(buid);
    }
    
    public void createEvents(String buid, List<EventDataDTO> events) {
        
        List<Block> cabBlocks = new ArrayList<>();
        List<Block> driverBlocks = new ArrayList<>();
        
        for (EventDataDTO event : events) {
            Block block = new Block(event.getId(), event.getStartTime(), event.getEndTime());
            if (event.getType().equals(Constant.CAB)) {
                block.setType(Constant.CAB);
                cabBlocks.add(block);
            } else if (event.getType().equals(Constant.DRIVER)) {
                block.setType(Constant.DRIVER);
                driverBlocks.add(block);
            }
        }
        
        cabBlocks.forEach(block -> block.setBuid(buid));
        driverBlocks.forEach(block -> block.setBuid(buid));
        blockRepository.saveAll(cabBlocks);
        blockRepository.saveAll(driverBlocks);
    }
    
    public List<RouteDetailDTO> getShuttleRoutes(String buid) {
        ShuttleAvailabilityDTO shuttleAvailabilityDTO = externalApiService.getShuttleRoutes(buid);
        if (Objects.isNull(shuttleAvailabilityDTO)) {
            throw new RuntimeException("Shuttle routes not available for buid: " + buid);
        }
        return ConversionUtil.convertToResponseDTO(shuttleAvailabilityDTO);
    }
}
