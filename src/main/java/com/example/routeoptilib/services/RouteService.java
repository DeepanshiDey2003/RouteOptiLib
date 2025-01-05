package com.example.routeoptilib.services;

import com.example.routeoptilib.models.CabDTO;
import com.example.routeoptilib.models.CabDriverUnit;
import com.example.routeoptilib.models.DriverDTO;
import com.example.routeoptilib.models.RouteDetailDTO;
import com.example.routeoptilib.models.RoutePart;
import com.example.routeoptilib.models.StopDetailDTO;
import com.example.routeoptilib.persistence.entity.Block;
import com.example.routeoptilib.models.BlockDataDTO;
import com.example.routeoptilib.models.OptimisedSuggestionDataDTO;
import com.example.routeoptilib.persistence.entity.CabDriverRouteMapping;
import com.example.routeoptilib.persistence.repository.BlockRepository;
import com.example.routeoptilib.persistence.repository.CabDriverRouteMappingRepository;
import com.example.routeoptilib.persistence.repository.CabRepository;
import com.example.routeoptilib.persistence.repository.DriverRepository;
import com.example.routeoptilib.persistence.repository.RouteRepository;
import com.example.routeoptilib.utils.Constant;
import com.example.routeoptilib.utils.ConversionUtil;
import com.example.routeoptilib.utils.LoadedBuidsChecker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.moveinsync.bus.models.dto.ShuttleAvailabilityDTO;
import com.moveinsync.ets.models.Duty;
import com.moveinsync.ets.models.EmptyLeg;
import com.moveinsync.tripsheet.models.TripsheetDTOV2;
import com.moveinsync.vehiclemanagementservice.models.VehicleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {
    private final ExternalApiService externalApiService;
    private final BlockRepository blockRepository;
    private final CabDriverRouteMappingRepository cabDriverRouteMappingRepository;
    private final DriverRepository driverRepository;
    private final RouteRepository routeRepository;
    private final CabRepository cabRepository;
    private final ScheduleService scheduleService;
    
    /**
     * @deprecated
     */
    @Deprecated(since = "UI-Integration", forRemoval = true)
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
        
        return Lists.newArrayList();
    }
    
    public List<OptimisedSuggestionDataDTO> suggestCabDriverForRoutes(String buid) {
        var cabs = getAllCabs(buid);
        var drivers = getAllDrivers(buid);
        var routes = getShuttleRoutes(buid);
        List<RoutePart> routeParts = routes.stream()
                .map(r -> {
                    RoutePart routePart = new RoutePart();
                    routePart.setAlreadyAssigned(StringUtils.isNotBlank(r.getAssignedCabIdentification()));
                    routePart.setId(r.getRouteId());
                    var firstStop = r.getStops().getFirst();
                    var lastStop = r.getStops().getLast();
                    routePart.setDistance(scheduleService.calculateDistance(firstStop.getGeoCord(), lastStop.getGeoCord()));
                    routePart.setStartPoint(firstStop.getGeoCord());
                    routePart.setEndPoint(lastStop.getGeoCord());
                    routePart.setStartTime(LocalTime.of((int) ((firstStop.getPlannedArrivalTime() / 60) % 12), (int) (firstStop.getPlannedArrivalTime() % 60)));
                    routePart.setEndTime(LocalTime.of((int) ((lastStop.getPlannedArrivalTime() / 60) % 12), (int) (lastStop.getPlannedArrivalTime() % 60)));
                    return routePart;
                })
                .collect(Collectors.toList());
        Map<CabDriverUnit, List<RoutePart>> map = scheduleService.scheduleCabs(cabs, drivers, routeParts);
        List<OptimisedSuggestionDataDTO> optimisedSuggestionDataDTOList = Lists.newArrayList();
        for (Map.Entry<CabDriverUnit, List<RoutePart>> entry : map.entrySet()) {
            var cabDriver = entry.getKey();
            entry.getValue().forEach(routePart -> {
                var dto = new OptimisedSuggestionDataDTO();
                dto.setRouteId(routePart.getId());
                dto.setDriverId(cabDriver.getDriverId());
                dto.setDriverName(cabDriver.getDriverName());
                dto.setVehicleIdentification(cabDriver.getCabId());
                optimisedSuggestionDataDTOList.add(dto);
            });
        }
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
    
    public List<DriverDTO> getAllDrivers(String buid) {
        var drivers = driverRepository.findAllByBuid(buid);
        List<DriverDTO> driverDTODTOS = ConversionUtil.convertToDriverDTO(drivers);
        if (CollectionUtils.isEmpty(driverDTODTOS) && LoadedBuidsChecker.isNotLoaded(buid, LoadedBuidsChecker.LoadType.DRIVER)) {
            log.info("Loading drivers from API for buid {}", buid);
            var externalDrivers = externalApiService.getDriverData(buid);
            var driverEntities = ConversionUtil.convertToDriverEntities(buid, externalDrivers);
            driverEntities = driverRepository.saveAll(driverEntities);
            driverDTODTOS = ConversionUtil.convertToDriverDTO(driverEntities);
        }
        if (CollectionUtils.isNotEmpty(driverDTODTOS)) {
            LoadedBuidsChecker.addLoadedBuid(buid, LoadedBuidsChecker.LoadType.DRIVER);
        }
        log.info("Loaded {} drivers from API for buid {}", driverDTODTOS.size(), buid);
        var blocks = blockRepository.findAllByBuidAndType(buid, Constant.DRIVER);
        Map<String, List<Block>> blockMap = Maps.newHashMap();
        for (Block block : blocks) {
            blockMap.computeIfAbsent(block.getBlockId(), k -> new ArrayList<>()).add(block);
        }
        for (DriverDTO driverDTO : driverDTODTOS) {
            var block = blockMap.get(driverDTO.getLicense());
            if (CollectionUtils.isEmpty(block)) {
                driverDTO.setBlocks(Lists.newArrayList());
                continue;
            }
            driverDTO.setBlocks(block.size() > 0 ? block : Lists.newArrayList());
        }
        return driverDTODTOS;
    }
    
    public List<CabDTO> getAllCabs(String buid) {
        var cabs = cabRepository.findByBuid(buid);
        List<CabDTO> cabDTOList = ConversionUtil.convertToCabDTO(cabs);
        if (CollectionUtils.isEmpty(cabDTOList) && LoadedBuidsChecker.isNotLoaded(buid, LoadedBuidsChecker.LoadType.CAB)) {
            log.info("Loading cabs from API for buid {}", buid);
            List<VehicleDTO> vehiclesByBuid = externalApiService.getVehiclesByBuid(buid);
            cabRepository.saveAll(ConversionUtil.convertToCabEntities(buid, vehiclesByBuid));
            cabDTOList = ConversionUtil.convertFromVehicleToCabDTO(vehiclesByBuid);
        }
        if (CollectionUtils.isNotEmpty(cabDTOList)) {
            LoadedBuidsChecker.addLoadedBuid(buid, LoadedBuidsChecker.LoadType.CAB);
        }
        log.info("Loaded {} cabs from API for buid {}", cabDTOList.size(), buid);
        var blocks = blockRepository.findAllByBuidAndType(buid, Constant.CAB);
        Map<String, List<Block>> blockMap = Maps.newHashMap();
        for (Block block : blocks) {
            blockMap.computeIfAbsent(block.getBlockId(), k -> new ArrayList<>()).add(block);
        }
        for (CabDTO cabDTO : cabDTOList) {
            var block = blockMap.get(cabDTO.getRegistration());
            if (CollectionUtils.isEmpty(block)) {
                cabDTO.setBlocks(Lists.newArrayList());
                continue;
            }
            cabDTO.setBlocks(block.size() > 0 ? block : Lists.newArrayList());
        }
        return cabDTOList;
    }
    
    public void createBlocks(String buid, List<BlockDataDTO> events) {
        
        List<Block> cabBlocks = new ArrayList<>();
        List<Block> driverBlocks = new ArrayList<>();
        
        for (BlockDataDTO event : events) {
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
        var routes = routeRepository.findByBuid(buid);
        List<RouteDetailDTO> result = ConversionUtil.convertToResponseDTO(routes);
        if (CollectionUtils.isEmpty(result) && LoadedBuidsChecker.isNotLoaded(buid, LoadedBuidsChecker.LoadType.ROUTE)) {
            log.info("No routes found for buid {}, loading from API", buid);
            result = populateRoutesFromAPI(buid);
            routeRepository.saveAll(ConversionUtil.convertToRouteEntities(buid, result));
        }
        if (CollectionUtils.isNotEmpty(result)) {
            LoadedBuidsChecker.addLoadedBuid(buid, LoadedBuidsChecker.LoadType.ROUTE);
        }
        log.info("Loaded {} routes from API for buid {}", result.size(), buid);
        var mappings = cabDriverRouteMappingRepository.findByBuid(buid);
        Map<String, CabDriverRouteMapping> routeIdMapping = mappings.stream()
                .collect(Collectors.toMap(
                        CabDriverRouteMapping::getRouteId, rm -> rm, (a, b) -> b
                ));
        for (RouteDetailDTO routeDetailDTO : result) {
            var assignment = routeIdMapping.get(routeDetailDTO.getRouteId());
            if (Objects.isNull(assignment)) {
                continue;
            }
            var driver = driverRepository.findByBuidAndLicense(buid, assignment.getDriverLicense());
            routeDetailDTO.setAssignedDriverId(assignment.getDriverLicense());
            routeDetailDTO.setAssignedDriverName(driver.getName());
            routeDetailDTO.setAssignedCabIdentification(assignment.getCabIdentification());
            
            routeDetailDTO.getStops().sort(Comparator.comparing(StopDetailDTO::getPlannedArrivalTime));
        }
        return result;
    }
    
    private List<RouteDetailDTO> populateRoutesFromAPI(String buid) {
        ShuttleAvailabilityDTO shuttleAvailabilityDTO = externalApiService.getShuttleRoutes(buid);
      return ConversionUtil.convertToResponseDTO(shuttleAvailabilityDTO);
    }
    
    public boolean createMapping(String buid, String routeId, String driverLicense, String cabId) {
        CabDriverRouteMapping cabDriverRouteMapping = new CabDriverRouteMapping();
        cabDriverRouteMapping.setRouteId(routeId);
        cabDriverRouteMapping.setDriverLicense(driverLicense);
        cabDriverRouteMapping.setCabIdentification(cabId);
        cabDriverRouteMapping.setBuid(buid);
        try {
            cabDriverRouteMappingRepository.save(cabDriverRouteMapping);
            return true;
        } catch (Exception e) {
            log.error("Error while creating mapping: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
    }
}
