package com.example.routeoptilib.persistence.repository;

import com.example.routeoptilib.persistence.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
  List<Route> findByBuid(String buid);
  
  @Transactional
  @Modifying
  @Query("DELETE FROM Route r WHERE r.buid = :buid")
  void deleteAllByBuid(@Param(value = "buid") String buid);
}
