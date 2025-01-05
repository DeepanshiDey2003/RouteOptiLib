package com.example.routeoptilib.persistence.repository;

import com.example.routeoptilib.persistence.entity.CabDriverRouteMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CabDriverRouteMappingRepository extends JpaRepository<CabDriverRouteMapping, Long> {
  List<CabDriverRouteMapping> findByBuidAndRouteId(String buid, String routeId);
  
  List<CabDriverRouteMapping> findByBuid(String buid);
  
  @Transactional
  @Modifying
  @Query("DELETE FROM CabDriverRouteMapping r WHERE r.buid = :buid")
  void deleteAllByBuid(@Param(value = "buid") String buid);
  
  @Query("SELECT c FROM CabDriverRouteMapping c WHERE c.buid = :buid AND c.routeId = :routeId")
  CabDriverRouteMapping getByRouteId(@Param(value = "buid") String buid, @Param(value = "routeId") String routeId);
}
