package com.example.routeoptilib.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "block")
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String blockId;
    private String type;
    private long startTime;
    private long endTime;
    
    public Block(String blockId, String type, long time, long time1) {
        this.blockId = blockId;
        this.type = type;
        this.startTime = time;
        this.endTime = time1;
    }
    
    public Block(String blockId, long startTime, long endTime) {
        this.blockId = blockId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
