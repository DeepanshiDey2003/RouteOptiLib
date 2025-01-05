package com.example.routeoptilib.persistence.repository;

import com.example.routeoptilib.persistence.entity.Cab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CabRepository extends JpaRepository<Cab, Long> {
  List<Cab> findByBuid(String buid);
  
  @Transactional
  @Modifying
  @Query("DELETE FROM Cab c WHERE c.buid = :buid")
  void deleteAllByBuid(@Param(value = "buid") String buid);
}
