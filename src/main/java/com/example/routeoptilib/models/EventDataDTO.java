package com.example.routeoptilib.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDataDTO {
    private String id;
    private String type;
    private long startTime;
    private long endTime;
}
