package com.example.routeoptilib.services;

import com.example.routeoptilib.models.CabDTO;
import com.example.routeoptilib.models.CabDriverUnit;
import com.example.routeoptilib.models.DriverDTO;
import com.example.routeoptilib.models.RoutePart;
import com.example.routeoptilib.persistence.entity.Block;
import com.mis.data.location.STW_Location;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
  // Global Properties
  public enum GlobalProperties {
    BREAK_DURATION_MINUTES(30),
    BUFFER_DEAD_LEG_TIME_PER_KM_MINUTES(15),
    MAX_STRETCH_HOURS(4),
    MAX_DUTY_HOURS_PER_DAY(12),
    LUNCH_BREAK_START_TIME_MINUTES_FROM_MIDNIGHT(780),
    LUNCH_BREAK_END_TIME_MINUTES_FROM_MIDNIGHT(860);
    
    @Getter
    int numValue;
    
    GlobalProperties(int numValue) {
      this.numValue = numValue;
    }
    
    public void setValue(int numValue) {
      this.numValue = numValue;
    }
  }
  
  /**
   * Schedules the routeParts intelligently for the available cabDTOS.
   *
   * @param cabDTOS  List of available cabDTOS
   * @param routeParts List of routeParts to be covered
   * @return A map of cab to its assigned routeParts
   */
  public Map<CabDriverUnit, List<RoutePart>> scheduleCabs(List<CabDTO> cabDTOS, List<DriverDTO> driverDTOS, List<RoutePart> routeParts) {
    Map<CabDriverUnit, List<RoutePart>> cabAssignments = new HashMap<>();
    Map<String, CabDriverUnit> assignedCabs = new HashMap<>();
    
    // Sort routeParts by start time for optimal scheduling
    routeParts.sort(Comparator.comparing(RoutePart::getStartTime));
    int driverIndex = 0;
    
    for (RoutePart routePart : routeParts) {
      CabDTO assignedCabDTO = null;
      for (CabDTO cabDTO : cabDTOS) {
        if (driverDTOS.size() <= driverIndex) {
          break;
        }
        CabDriverUnit cabDriverUnit = new CabDriverUnit();
        if (assignedCabs.containsKey(cabDTO.getRegistration())) {
          cabDriverUnit = assignedCabs.get(cabDTO.getRegistration());
        } else {
          cabDriverUnit.setCabId(cabDTO.getRegistration());
          cabDriverUnit.setDriverId(driverDTOS.get(driverIndex).getLicense());
          cabDriverUnit.setDriverName(driverDTOS.get(driverIndex).getName());
          cabDriverUnit.setBlocks(driverDTOS.get(driverIndex).getBlocks());
          cabDriverUnit.getBlocks().addAll(cabDTO.getBlocks());
        }
        if (canAssignRoute(cabDTO, routePart, cabAssignments.getOrDefault(cabDriverUnit, new ArrayList<>()))) {
          cabAssignments.computeIfAbsent(cabDriverUnit, k -> new ArrayList<>()).add(routePart);
          cabDTO.getBlocks().add(new Block(cabDTO.getRegistration(), getLongFromLocalTime(routePart.getStartTime()), getLongFromLocalTime(routePart.getEndTime())));
          assignedCabDTO = cabDTO;
          assignedCabs.put(cabDriverUnit.getCabId(), cabDriverUnit);
          driverIndex++;
          break;
        }
      }
      
      if (assignedCabDTO == null) {
        log.warn("No cab available for routePart from {} to {} starting at {}", routePart.getStartPoint(), routePart.getEndPoint(), routePart.getStartTime());
      }
    }
    
    return cabAssignments;
  }
  
  private static long getLongFromLocalTime(LocalTime time) {
    return time.getHour() * 60 + (long)(time.getMinute());
  }
  
  /**
   * Checks if a cabDTO can be assigned a routePart without violating any constraints.
   *
   * @param cabDTO           The cabDTO to check
   * @param routePart         The routePart to be assigned
   * @param assignedRouteParts Already assigned routes for the cabDTO
   * @return True if the cabDTO can be assigned the routePart, false otherwise
   */
  private boolean canAssignRoute(CabDTO cabDTO, RoutePart routePart, List<RoutePart> assignedRouteParts) {
    
    for (Block block : cabDTO.getBlocks()) {
      if (block.overlapsWith(routePart.getStartTime(), routePart.getEndTime())) {
        return false;
      }
    }
    
    // Calculate total duty time for the day
    int totalDutyMinutes = assignedRouteParts.stream()
            .mapToInt(r -> Math.toIntExact(Duration.between(r.getStartTime(), r.getEndTime()).toMinutes()))
            .sum();
    
    if (totalDutyMinutes + Duration.between(routePart.getStartTime(), routePart.getEndTime()).toMinutes() > (GlobalProperties.MAX_DUTY_HOURS_PER_DAY.getNumValue()) * 60) {
      return false;
    }
    
    // Check for maximum stretch time and break duration
    if (!assignedRouteParts.isEmpty()) {
      RoutePart lastRoutePart = assignedRouteParts.get(assignedRouteParts.size() - 1);
      long stretchMinutes = Duration.between(lastRoutePart.getStartTime(), routePart.getEndTime()).toMinutes();
      
      if (stretchMinutes > GlobalProperties.MAX_STRETCH_HOURS.getNumValue() * 60) {
        return false;
      }
      
      // Ensure break duration after stretch
      if (stretchMinutes > GlobalProperties.MAX_STRETCH_HOURS.getNumValue() * 60 &&
              Duration.between(lastRoutePart.getEndTime(), routePart.getStartTime()).toMinutes() < GlobalProperties.BREAK_DURATION_MINUTES.getNumValue()) {
        return false;
      }
      
      // Add buffer time for dead leg
      double distanceBetweenRoutes = calculateDistance(lastRoutePart.getEndPoint(), routePart.getStartPoint());
      long bufferTime = (long) (distanceBetweenRoutes * GlobalProperties.BUFFER_DEAD_LEG_TIME_PER_KM_MINUTES.getNumValue());
      
      if (Duration.between(lastRoutePart.getEndTime(), routePart.getStartTime()).toMinutes() < bufferTime) {
        return false;
      }
    }
    LocalTime LUNCH_BREAK_START = LocalTime.of(
            (GlobalProperties.LUNCH_BREAK_START_TIME_MINUTES_FROM_MIDNIGHT.numValue / 60) % 24,
            GlobalProperties.LUNCH_BREAK_START_TIME_MINUTES_FROM_MIDNIGHT.numValue % 60);
    LocalTime LUNCH_BREAK_END = LocalTime.of(
            (GlobalProperties.LUNCH_BREAK_END_TIME_MINUTES_FROM_MIDNIGHT.numValue / 60) % 24,
            GlobalProperties.LUNCH_BREAK_END_TIME_MINUTES_FROM_MIDNIGHT.numValue % 60);
    // Ensure no routes during lunch break
    if (routePart.getStartTime().isBefore(LUNCH_BREAK_END) && routePart.getEndTime().isAfter(LUNCH_BREAK_START)) {
      return false;
    }
    
    return true;
  }
  
  /**
   * Calculates the distance between two geocoordinates (dummy implementation).
   *
   * @param pointA Starting point
   * @param pointB Ending point
   * @return Distance in kilometers
   */
  public double calculateDistance(String pointA, String pointB) {
    // Replace this with actual distance calculation logic
    return new STW_Location(pointA).distance(new STW_Location(pointB)) / 1000D;
  }
}
