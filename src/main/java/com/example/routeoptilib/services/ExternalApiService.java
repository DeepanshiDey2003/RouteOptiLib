package com.example.routeoptilib.services;

import com.example.routeoptilib.models.Cabby;
import com.example.routeoptilib.utils.Constant;
import com.google.gson.JsonSyntaxException;
import com.mis.serverdata.utils.GsonUtils;
import com.moveinsync.MisBuidUtils;
import com.moveinsync.bus.models.dto.ShuttleAvailabilityDTO;
import com.moveinsync.ets.models.Duty;
import com.moveinsync.tripsheet.models.TripsheetDTOV2;
import com.moveinsync.vehiclemanagementservice.models.VehicleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Service
public class ExternalApiService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${vms.api.url}")
    private String vmsApiUrl;
    
    @Value("${tripsheet.service.api.url}")
    private String tripsheetServiceApiUrl;
    
    @Value("${x.mis.token.ets}")
    private String xMisTokenEts;
    
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
    
    public List<Cabby> getDriverData(String buid) {
        String url = String.format("%s%s", MisBuidUtils.getUrlOfBuid(buid), "ets/apis/cabbies");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Constant.X_MIS_TOKEN, xMisTokenEts);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<List<Cabby>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody();
        } catch (RestClientException e) {
            return Collections.emptyList();
        }
    }

    public ShuttleAvailabilityDTO getShuttleRoutes(String buid) {
        String url = String.format("%s%s", MisBuidUtils.getUrlOfBuid(buid), "ets/shuttle-service/routes");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Constant.X_MIS_TOKEN, xMisTokenEts);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return GsonUtils.getGson().fromJson(response.getBody(), ShuttleAvailabilityDTO.class);
            } else {
                return null;
            }
        } catch (RestClientException | JsonSyntaxException e) {
            return null;
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
    
    public Duty getDutyForTrip(String buid, String guid) {
        String url = String.format("%s%s/%s", MisBuidUtils.getUrlOfBuid(buid), "ets/apis/getTripDuty", guid);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Constant.X_MIS_TOKEN, xMisTokenEts);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Duty> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Duty.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                return null;
            }
        } catch (RestClientException e) {
            return null;
        }
    }
}
