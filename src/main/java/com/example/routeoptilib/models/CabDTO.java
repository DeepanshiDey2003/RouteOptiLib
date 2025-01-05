package com.example.routeoptilib.models;

import com.example.routeoptilib.persistence.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CabDTO {
  private int id;
  private String registration;
  private List<Block> blocks;
  
  public CabDTO(int id, String registration) {
    this.id = id;
    this.registration = registration;
  }
}
