package com.example.routeoptilib.services;

import com.example.routeoptilib.models.Cab;
import com.example.routeoptilib.models.RoutePart;
import com.mis.data.location.STW_Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

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
  
  Logger log = LoggerFactory.getLogger(ScheduleService.class);
  
  /**
   * Schedules the routeParts intelligently for the available cabs.
   *
   * @param cabs  List of available cabs
   * @param routeParts List of routeParts to be covered
   * @return A map of cab to its assigned routeParts
   */
  public Map<Cab, List<RoutePart>> scheduleCabs(List<Cab> cabs, List<RoutePart> routeParts) {
    Map<Cab, List<RoutePart>> cabAssignments = new HashMap<>();
    
    // Sort routeParts by start time for optimal scheduling
    routeParts.sort(Comparator.comparing(RoutePart::getStartTime));
    
    for (RoutePart routePart : routeParts) {
      Cab assignedCab = null;
      
      for (Cab cab : cabs) {
        if (canAssignRoute(cab, routePart, cabAssignments.getOrDefault(cab, new ArrayList<>()))) {
          cabAssignments.computeIfAbsent(cab, k -> new ArrayList<>()).add(routePart);
          assignedCab = cab;
          break;
        }
      }
      
      if (assignedCab == null) {
        log.warn("No cab available for routePart from {} to {} starting at {}", routePart.getStartPoint(), routePart.getEndPoint(), routePart.getStartTime());
      }
    }
    
    return cabAssignments;
  }
  
  /**
   * Checks if a cab can be assigned a routePart without violating any constraints.
   *
   * @param cab           The cab to check
   * @param routePart         The routePart to be assigned
   * @param assignedRouteParts Already assigned routes for the cab
   * @return True if the cab can be assigned the routePart, false otherwise
   */
  private boolean canAssignRoute(Cab cab, RoutePart routePart, List<RoutePart> assignedRouteParts) {
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
  private double calculateDistance(String pointA, String pointB) {
    // Replace this with actual distance calculation logic
    return new STW_Location(pointA).distance(new STW_Location(pointB)) / 1000D;
  }
}
