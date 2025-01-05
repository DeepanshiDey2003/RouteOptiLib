package com.example.routeoptilib.models;

import com.example.routeoptilib.persistence.entity.Block;
import lombok.Data;

import java.util.List;

@Data
public class DriverDTO {
  Long id;
  String license;
  String name;
  List<Block> blocks;
}
