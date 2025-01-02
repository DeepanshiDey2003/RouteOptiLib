package com.example.routeoptilib.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Block {
    private String blockId;
    private long startTime;
    private long endTime;
    
    public Block(String guid, long time, long time1) {
        this.blockId = guid;
        this.startTime = time;
        this.endTime = time1;
    }
}
