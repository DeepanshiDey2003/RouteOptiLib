package com.example.routeoptilib.persistence.repository;

import com.example.routeoptilib.persistence.entity.CabDriverRouteMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CabDriverRouteMappingRepository extends JpaRepository<CabDriverRouteMapping, Long> {
  List<CabDriverRouteMapping> findByBuidAndRouteId(String buid, String routeId);
  
  List<CabDriverRouteMapping> findByBuid(String buid);
}
