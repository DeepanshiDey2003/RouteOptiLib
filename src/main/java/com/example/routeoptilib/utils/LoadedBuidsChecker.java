package com.example.routeoptilib.utils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public class LoadedBuidsChecker {
  private static Map<LoadType, Set<String>> loadedBuids = Maps.newHashMap();
  
  public enum LoadType {
    ROUTE, CAB, DRIVER
  }
  
  public static void addLoadedBuid(String buid, LoadType loadType) {
    loadedBuids.computeIfAbsent(loadType, k -> Sets.newHashSet()).add(buid);
  }
  
  public static boolean isLoaded(String buid, LoadType loadType) {
    return loadedBuids.getOrDefault(loadType, Sets.newHashSet()).contains(buid);
  }
  
  public static boolean isNotLoaded(String buid, LoadType loadType) {
    return !isLoaded(buid, loadType);
  }
}
