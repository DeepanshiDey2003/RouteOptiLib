package com.example.routeoptilib.config;

import com.example.routeoptilib.adapters.LocalTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    @Primary
    public Gson gson() {
        return new GsonBuilder().registerTypeAdapter(LocalTime.class, new LocalTimeAdapter()).create();
    }
}
