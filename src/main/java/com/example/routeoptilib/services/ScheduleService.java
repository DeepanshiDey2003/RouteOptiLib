package com.example.routeoptilib.services;

import com.example.routeoptilib.models.Cab;
import com.example.routeoptilib.models.Route;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private static int BREAK_DURATION_MINUTES = 60;
  private static int BUFFER_DEAD_LEG_TIME_PER_KM_MINUTES = 15;
  private static int MAX_STRETCH_HOURS = 4;
  private static int MAX_DUTY_HOURS_PER_DAY = 12;
  
  /**
   * Schedules the routes intelligently for the available cabs.
   *
   * @param cabs  List of available cabs
   * @param routes List of routes to be covered
   * @return A map of cab to its assigned routes
   */
  public Map<Cab, List<Route>> scheduleCabs(List<Cab> cabs, List<Route> routes) {
    Map<Cab, List<Route>> cabAssignments = new HashMap<>();
    
    // Sort routes by start time for optimal scheduling
    routes.sort(Comparator.comparing(Route::getStartTime));
    
    for (Route route : routes) {
      Cab assignedCab = null;
      
      for (Cab cab : cabs) {
        if (canAssignRoute(cab, route, cabAssignments.getOrDefault(cab, new ArrayList<>()))) {
          cabAssignments.computeIfAbsent(cab, k -> new ArrayList<>()).add(route);
          assignedCab = cab;
          break;
        }
      }
      
      if (assignedCab == null) {
        log.warn("No cab available for route from {} to {} starting at {}", route.getStartPoint(), route.getEndPoint(), route.getStartTime());
      }
    }
    
    return cabAssignments;
  }
  
  /**
   * Checks if a cab can be assigned a route without violating any constraints.
   *
   * @param cab           The cab to check
   * @param route         The route to be assigned
   * @param assignedRoutes Already assigned routes for the cab
   * @return True if the cab can be assigned the route, false otherwise
   */
  private boolean canAssignRoute(Cab cab, Route route, List<Route> assignedRoutes) {
    // Calculate total duty time for the day
    int totalDutyMinutes = assignedRoutes.stream()
            .mapToInt(r -> Math.toIntExact(Duration.between(r.getStartTime(), r.getEndTime()).toMinutes()))
            .sum();
    
    if (totalDutyMinutes + Duration.between(route.getStartTime(), route.getEndTime()).toMinutes() > MAX_DUTY_HOURS_PER_DAY * 60) {
      return false;
    }
    
    // Check for maximum stretch time and break duration
    if (!assignedRoutes.isEmpty()) {
      Route lastRoute = assignedRoutes.get(assignedRoutes.size() - 1);
      long stretchMinutes = Duration.between(lastRoute.getStartTime(), route.getEndTime()).toMinutes();
      
      if (stretchMinutes > MAX_STRETCH_HOURS * 60) {
        return false;
      }
      
      // Ensure break duration after stretch
      if (stretchMinutes > MAX_STRETCH_HOURS * 60 &&
              Duration.between(lastRoute.getEndTime(), route.getStartTime()).toMinutes() < BREAK_DURATION_MINUTES) {
        return false;
      }
      
      // Add buffer time for dead leg
      double distanceBetweenRoutes = calculateDistance(lastRoute.getEndPoint(), route.getStartPoint());
      long bufferTime = (long) (distanceBetweenRoutes * BUFFER_DEAD_LEG_TIME_PER_KM_MINUTES);
      
      if (Duration.between(lastRoute.getEndTime(), route.getStartTime()).toMinutes() < bufferTime) {
        return false;
      }
    }
    
    // Ensure no routes during lunch break
    if (route.getStartTime().isBefore(LUNCH_BREAK_END) && route.getEndTime().isAfter(LUNCH_BREAK_START)) {
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
    return 10.0; // Dummy value
  }
}
