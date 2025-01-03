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
}
