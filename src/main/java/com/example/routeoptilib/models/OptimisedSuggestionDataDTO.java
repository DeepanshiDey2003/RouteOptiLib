package com.example.routeoptilib.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptimisedSuggestionDataDTO {
    private String routeId;
    private String vehicle;
    private String driver;
}