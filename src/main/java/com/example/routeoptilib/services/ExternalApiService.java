package com.example.routeoptilib.services;

import com.example.routeoptilib.utils.Constant;
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

import java.util.List;

@Service
public class ExternalApiService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${vms.api.url}")
    private String vmsApiUrl;
    
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
}
