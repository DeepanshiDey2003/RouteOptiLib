package com.example.routeoptilib.persistence.repository;

import com.example.routeoptilib.persistence.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, String> {
  
  List<Driver> findAllByBuid(String buid);
  List<Driver> findByBuidAndLicense(String buid, String license);
  
  @Transactional
  @Modifying
  @Query("DELETE FROM Driver d WHERE d.buid = :buid")
  void deleteAllByBuid(@Param(value = "buid") String buid);
}
