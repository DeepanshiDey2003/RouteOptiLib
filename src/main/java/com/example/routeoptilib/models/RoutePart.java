package com.example.routeoptilib.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePart {
  String id;
  String startPoint;
  String endPoint;
  LocalTime startTime;
  LocalTime endTime;
  double distance;
  boolean alreadyAssigned;
  
  public LocalTime getStartTime() {
    return startTime;
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public double getDistance() {
    return distance;
  }
  
  public void setDistance(double distance) {
    this.distance = distance;
  }
  
  public LocalTime getEndTime() {
    return endTime;
  }
  
  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }
  
  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }
  
  public String getEndPoint() {
    return endPoint;
  }
  
  public void setEndPoint(String endPoint) {
    this.endPoint = endPoint;
  }
  
  public String getStartPoint() {
    return startPoint;
  }
  
  public void setStartPoint(String startPoint) {
    this.startPoint = startPoint;
  }
}
