package com.example.routeoptilib.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteDetailDTO {
    private String routeId;
    private String routeName;
    private List<StopDetailDTO> stops;
    
    public RouteDetailDTO(String routeId, String routeName, List<StopDetailDTO> stops) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.stops = stops;
    }
}
