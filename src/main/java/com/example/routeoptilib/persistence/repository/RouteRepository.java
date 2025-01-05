package com.example.routeoptilib.persistence.repository;

import com.example.routeoptilib.persistence.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, String> {
  List<Route> findByBuid(String buid);
}
