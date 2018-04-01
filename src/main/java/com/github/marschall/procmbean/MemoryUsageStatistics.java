package com.github.marschall.procmbean;

import javax.management.openmbean.CompositeData;

public final class MemoryUsageStatistics {

  private final long totalProgram;
  private final long residentSet;
  private final long residentShared;
  private final long text;
  private final long data;

  MemoryUsageStatistics(
          long totalProgram,
          long residentSet,
          long residentShared,
          long text,
          long data) {
    this.totalProgram = totalProgram;
    this.residentSet = residentSet;
    this.residentShared = residentShared;
    this.text = text;
    this.data = data;
  }

  public static MemoryUsageStatistics from(CompositeData compositeData) {
    return new MemoryUsageStatistics(
            (Long) compositeData.get("totalProgram"),
            (Long) compositeData.get("residentSet"),
            (Long) compositeData.get("residentShared"),
            (Long) compositeData.get("text"),
            (Long) compositeData.get("data"));
  }

  @Units("bytes")
  public long getTotalProgram() {
    return this.totalProgram;
  }

  @Units("bytes")
  public long getResidentSet() {
    return this.residentSet;
  }

  @Units("bytes")
  public long getResidentShared() {
    return this.residentShared;
  }

  @Units("bytes")
  public long getText() {
    return this.text;
  }

  @Units("bytes")
  public long getData() {
    return this.data;
  }

}
