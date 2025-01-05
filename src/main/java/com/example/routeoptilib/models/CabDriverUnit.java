package com.example.routeoptilib.models;

import com.example.routeoptilib.persistence.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CabDriverUnit {
  private String driverId;
  private String driverName;
  private String cabId;
  private List<Block> blocks;
  
  @Override
  public int hashCode() {
    return (driverId + driverName + cabId).hashCode();
  }
}
