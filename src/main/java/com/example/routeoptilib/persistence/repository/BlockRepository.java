package com.example.routeoptilib.persistence.repository;

import com.example.routeoptilib.persistence.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Integer> {
    
    List<Block> findAllByBlockId(String blockId);
    List<Block> findAllByBlockIdIn(List<String> blockIds);
    
    
}
