package com.example.routeoptilib.utils;

import com.example.routeoptilib.models.CabDTO;
import com.example.routeoptilib.models.Cabby;
import com.example.routeoptilib.models.DriverDTO;
import com.example.routeoptilib.models.RouteDetailDTO;
import com.example.routeoptilib.models.StopDetailDTO;
import com.example.routeoptilib.persistence.entity.Cab;
import com.example.routeoptilib.persistence.entity.Driver;
import com.example.routeoptilib.persistence.entity.Route;
import com.mis.serverdata.utils.GsonUtils;
import com.moveinsync.bus.models.dto.ShuttleAvailabilityDTO;
import com.moveinsync.bus.models.dto.ShuttleStopDTO;
import com.moveinsync.vehiclemanagementservice.models.VehicleDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConversionUtil {
    
    ConversionUtil() {
    }
    
    public static List<RouteDetailDTO> convertToResponseDTO(ShuttleAvailabilityDTO availabilityDTO) {
        return availabilityDTO.getShuttleRoutes().stream()
                .filter(r -> r.getShuttleStopsTimingMap().size() >= 2).map(route -> {
            List<StopDetailDTO> stopDetails = route.getShuttleStopsTimingMap().entrySet().stream()
                    .map(entry -> {
                        String stopGuid = entry.getKey();
                        Integer plannedArrivalTime = entry.getValue();

                        ShuttleStopDTO stop = availabilityDTO.getShuttleStops().get(stopGuid);
                            return new StopDetailDTO(
                                    stop.getGuid(),
                                    StringUtils.defaultIfBlank(stop.getRouteName(), UUID.randomUUID().toString().substring(0, 8)),
                                    stop.getGeoCord(),
                                    plannedArrivalTime
                            );
                    }).collect(Collectors.toList());
            stopDetails.sort(Comparator.comparing(StopDetailDTO::getPlannedArrivalTime));
            if (stopDetails.getLast().getPlannedArrivalTime() - stopDetails.getFirst().getPlannedArrivalTime() >= 240) {
              int factor = 0;
              long initialStopTime = stopDetails.getFirst().getPlannedArrivalTime();
              int divisions = stopDetails.size() - 1;
              for (StopDetailDTO stopDetail : stopDetails) {
                stopDetail.setPlannedArrivalTime(initialStopTime + (long) (240 / divisions) * (factor++));
              }
            }
            return new RouteDetailDTO(route.getRouteId(), route.getRouteName(), stopDetails);
        }).collect(Collectors.toList());
    }
    
    public static List<RouteDetailDTO> convertToResponseDTO(List<Route> routes) {
      if (CollectionUtils.isEmpty(routes)) {
        return new ArrayList<>();
      }
      return routes.stream()
              .map(r -> new RouteDetailDTO(
                      r.getRouteId(),
                      r.getName(),
                      Arrays.stream(GsonUtils.getGson().fromJson(r.getStops(), StopDetailDTO[].class)).collect(Collectors.toList())))
              .collect(Collectors.toList());
    }
  
  public static List<Route> convertToRouteEntities(String buid, List<RouteDetailDTO> result) {
      if (CollectionUtils.isEmpty(result)) {
        return new ArrayList<>();
      }
      return result.stream()
              .map(r -> {
                Route route = new Route();
                route.setRouteId(r.getRouteId());
                route.setName(r.getRouteName());
                route.setStops(GsonUtils.getGson().toJson(r.getStops()));
                route.setBuid(buid);
                return route;
              })
              .collect(Collectors.toList());
  }
  
  public static List<CabDTO> convertToCabDTO(List<Cab> cabs) {
      if (CollectionUtils.isEmpty(cabs)) {
        return new ArrayList<>();
      }
      return cabs.stream().map(c -> new CabDTO(c.getId().intValue(), c.getRegistration())).collect(Collectors.toList());
  }
  
  public static List<CabDTO> convertFromVehicleToCabDTO(List<VehicleDTO> vehicles) {
      if (CollectionUtils.isEmpty(vehicles)) {
        return new ArrayList<>();
      }
      return vehicles.stream().map(v -> new CabDTO(v.getNativeVehicleId(), v.getRegistration())).collect(Collectors.toList());
  }
  
  public static List<Cab> convertToCabEntities(String buid, List<VehicleDTO> vehiclesByBuid) {
      if (CollectionUtils.isEmpty(vehiclesByBuid)) {
        return new ArrayList<>();
      }
      return vehiclesByBuid.stream()
              .map(v -> {
                Cab cab = new Cab();
                cab.setBuid(buid);
                cab.setRegistration(v.getRegistration());
                cab.setCabId(v.getVehicleDisplayId());
                return cab;
              })
              .collect(Collectors.toList());
  }
  
  public static List<DriverDTO> convertToDriverDTO(List<Driver> drivers) {
      if (CollectionUtils.isEmpty(drivers)) {
        return new ArrayList<>();
      }
      return drivers.stream()
              .map(d -> {
                DriverDTO dto = new DriverDTO();
                dto.setId(d.getId());
                dto.setName(d.getName());
                dto.setLicense(d.getLicense());
                return dto;
              })
              .collect(Collectors.toList());
  }
  
  public static List<Driver> convertToDriverEntities(String buid, List<Cabby> drivers) {
    if (CollectionUtils.isEmpty(drivers)) {
      return new ArrayList<>();
    }
    return drivers.stream()
            .map(d -> {
              Driver dto = new Driver();
              dto.setName(d.getName());
              dto.setLicense(d.getLicence().getIdentification().getNumber());
              dto.setBuid(buid);
              return dto;
            })
            .collect(Collectors.toList());
  }
}
