package com.example.routeoptilib.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopDetailDTO {
    private String stopId;
    private String stopName;
    private String geoCord;
    private long plannedArrivalTime;
    
    public StopDetailDTO(String stopId, String stopName, String geoCord, long plannedArrivalTime) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.geoCord = geoCord;
        this.plannedArrivalTime = plannedArrivalTime;
    }
}
