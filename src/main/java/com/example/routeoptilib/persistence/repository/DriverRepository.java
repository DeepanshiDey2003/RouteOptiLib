package com.example.routeoptilib.persistence.repository;

import com.example.routeoptilib.persistence.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, String> {
  
  List<Driver> findAllByBuid(String buid);
  Driver findByBuidAndLicense(String buid, String license);
}
