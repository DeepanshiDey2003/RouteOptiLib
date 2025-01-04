package com.example.routeoptilib.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

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
    private String buid;
    
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
    
    public String getBuid() {
        return buid;
    }
    
    public void setBuid(String buid) {
        this.buid = buid;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getBlockId() {
        return blockId;
    }
    
    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
  
  public boolean overlapsWith(LocalTime durationStart, LocalTime durationEnd) {
        var blockStartTime = LocalTime.of((int) (this.startTime / 60), (int) (this.startTime % 60));
        return blockStartTime.isAfter(durationStart) && blockStartTime.isBefore(durationEnd);
  }
}
