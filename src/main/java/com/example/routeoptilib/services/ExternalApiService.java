package com.example.routeoptilib.services;

import com.example.routeoptilib.utils.Constant;
import com.moveinsync.tripsheet.models.TripsheetDTOV2;
import com.moveinsync.vehiclemanagementservice.models.VehicleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class ExternalApiService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${vms.api.url}")
    private String vmsApiUrl;
    
    @Value("${tripsheet.service.api.url}")
    private String tripsheetServiceApiUrl;
    
    public List<VehicleDTO> getVehiclesByBuid(String buid) {
        String url = String.format("%s/%s/vehicles", vmsApiUrl, buid);
        HttpHeaders headers = new HttpHeaders();
        headers.set(Constant.X_WIS_TOKEN, Constant.X_WIS_STATIC_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<List<VehicleDTO>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
            });
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<TripsheetDTOV2> getVehicleTripsForTheDay(String vehicleRegistration, long startTimestampOfDay, long endTimestampOfDay) {
        String url = UriComponentsBuilder.fromHttpUrl(tripsheetServiceApiUrl)
                .queryParam("cabids", vehicleRegistration)
                .queryParam("fromtime", startTimestampOfDay)
                .queryParam("totime", endTimestampOfDay)
                .toUriString();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer YOUR_API_TOKEN");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<List<TripsheetDTOV2>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
}
