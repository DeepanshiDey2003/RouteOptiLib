package com.example.routeoptilib.models;

import com.example.routeoptilib.persistence.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cab {
  private int id;
  private String registration;
  private String cabId;
  private List<Block> blocks;
}
