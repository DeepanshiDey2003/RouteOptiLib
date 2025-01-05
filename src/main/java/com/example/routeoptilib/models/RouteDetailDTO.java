package com.example.routeoptilib.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class RouteDetailDTO {
    private String routeId;
    private String routeName;
    private List<StopDetailDTO> stops;
    private String assignedDriverId;
    private String assignedDriverName;
    private String assignedCabIdentification;
    
    public RouteDetailDTO(String routeId, String routeName, List<StopDetailDTO> stops) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.stops = stops;
    }
}
