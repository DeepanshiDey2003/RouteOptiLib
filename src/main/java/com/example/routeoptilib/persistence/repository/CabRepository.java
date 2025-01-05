package com.example.routeoptilib.persistence.repository;

import com.example.routeoptilib.persistence.entity.Cab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CabRepository extends JpaRepository<Cab, Long> {
  List<Cab> findByBuid(String buid);
}
