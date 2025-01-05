package com.example.routeoptilib.services;

import com.example.routeoptilib.models.CabDTO;
import com.example.routeoptilib.models.CabDriverUnit;
import com.example.routeoptilib.models.DriverDTO;
import com.example.routeoptilib.models.RoutePart;
import com.example.routeoptilib.persistence.entity.Block;
import com.mis.data.location.STW_Location;
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
  private static LocalTime LUNCH_BREAK_START = LocalTime.of(13, 0);
  private static LocalTime LUNCH_BREAK_END = LocalTime.of(14, 0);
  private static int BREAK_DURATION_MINUTES = 30;
  private static int BUFFER_DEAD_LEG_TIME_PER_KM_MINUTES = 15;
  private static int MAX_STRETCH_HOURS = 4;
  private static int MAX_DUTY_HOURS_PER_DAY = 12;
  
  /**
   * Schedules the routeParts intelligently for the available cabDTOS.
   *
   * @param cabDTOS  List of available cabDTOS
   * @param routeParts List of routeParts to be covered
   * @return A map of cab to its assigned routeParts
   */
  public Map<CabDriverUnit, List<RoutePart>> scheduleCabs(List<CabDTO> cabDTOS, List<DriverDTO> driverDTOS, List<RoutePart> routeParts) {
    Map<CabDriverUnit, List<RoutePart>> cabAssignments = new HashMap<>();
    
    // Sort routeParts by start time for optimal scheduling
    routeParts.sort(Comparator.comparing(RoutePart::getStartTime));
    
    for (RoutePart routePart : routeParts) {
      CabDTO assignedCabDTO = null;
      for (CabDTO cabDTO : cabDTOS) {
        if (driverDTOS.isEmpty()) {
          break;
        }
        CabDriverUnit cabDriverUnit = new CabDriverUnit();
        cabDriverUnit.setCabId(cabDTO.getRegistration());
        cabDriverUnit.setDriverId(driverDTOS.get(0).getLicense());
        cabDriverUnit.setDriverName(driverDTOS.get(0).getName());
        if (canAssignRoute(cabDTO, routePart, cabAssignments.getOrDefault(cabDriverUnit, new ArrayList<>()))) {
          cabAssignments.computeIfAbsent(cabDriverUnit, k -> new ArrayList<>()).add(routePart);
          assignedCabDTO = cabDTO;
          driverDTOS.removeFirst();
          break;
        }
      }
      
      if (assignedCabDTO == null) {
        log.warn("No cab available for routePart from {} to {} starting at {}", routePart.getStartPoint(), routePart.getEndPoint(), routePart.getStartTime());
      }
    }
    
    return cabAssignments;
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
    
    if (totalDutyMinutes + Duration.between(routePart.getStartTime(), routePart.getEndTime()).toMinutes() > MAX_DUTY_HOURS_PER_DAY * 60) {
      return false;
    }
    
    // Check for maximum stretch time and break duration
    if (!assignedRouteParts.isEmpty()) {
      RoutePart lastRoutePart = assignedRouteParts.get(assignedRouteParts.size() - 1);
      long stretchMinutes = Duration.between(lastRoutePart.getStartTime(), routePart.getEndTime()).toMinutes();
      
      if (stretchMinutes > MAX_STRETCH_HOURS * 60) {
        return false;
      }
      
      // Ensure break duration after stretch
      if (stretchMinutes > MAX_STRETCH_HOURS * 60 &&
              Duration.between(lastRoutePart.getEndTime(), routePart.getStartTime()).toMinutes() < BREAK_DURATION_MINUTES) {
        return false;
      }
      
      // Add buffer time for dead leg
      double distanceBetweenRoutes = calculateDistance(lastRoutePart.getEndPoint(), routePart.getStartPoint());
      long bufferTime = (long) (distanceBetweenRoutes * BUFFER_DEAD_LEG_TIME_PER_KM_MINUTES);
      
      if (Duration.between(lastRoutePart.getEndTime(), routePart.getStartTime()).toMinutes() < bufferTime) {
        return false;
      }
    }
    
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
