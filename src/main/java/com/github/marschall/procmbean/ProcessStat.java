package com.github.marschall.procmbean;

import javax.management.openmbean.CompositeData;

public final class ProcessStat {

  private final int pid;
  private final char state;
  private final long minorFaults;
  private final long majorFaults;
  private final long userTime;
  private final long kernelTime;
  private final int threads;
  private final long virtualMemorySize;
  private final long residentSetSize;
  private final long softLimit;
  private final long pagesSwapped;
  private final long aggregatedBlockIoDelays;
  private final long guestTime;

  ProcessStat(
          int pid,
          char state,
          long minorFaults,
          long majorFaults,
          long userTime,
          long kernelTime,
          int threads,
          long virtualMemorySize,
          long residentSetSize,
          long softLimit,
          long pagesSwapped,
          long aggregatedBlockIoDelays,
          long guestTime) {
    this.pid = pid;
    this.state = state;
    this.minorFaults = minorFaults;
    this.majorFaults = majorFaults;
    this.userTime = userTime;
    this.kernelTime = kernelTime;
    this.threads = threads;
    this.virtualMemorySize = virtualMemorySize;
    this.residentSetSize = residentSetSize;
    this.softLimit = softLimit;
    this.pagesSwapped = pagesSwapped;
    this.aggregatedBlockIoDelays = aggregatedBlockIoDelays;
    this.guestTime = guestTime;
  }

  public static ProcessStat from(CompositeData compositeData) {
    return new ProcessStat(
            (Integer) compositeData.get("pid"),
            (Character) compositeData.get("state"),
            (Long) compositeData.get("minorFaults"),
            (Long) compositeData.get("majorFaults"),
            (Long) compositeData.get("userTime"),
            (Long) compositeData.get("kernelTime"),
            (Integer) compositeData.get("threads"),
            (Long) compositeData.get("virtualMemorySize"),
            (Long) compositeData.get("residentSetSize"),
            (Long) compositeData.get("softLimit"),
            (Long) compositeData.get("pagesSwapped"),
            (Long) compositeData.get("aggregatedBlockIoDelays"),
            (Long) compositeData.get("guestTime"));
  }

  public int getPid() {
    return this.pid;
  }

  public char getState() {
    return this.state;
  }

  public long getMinorFaults() {
    return this.minorFaults;
  }

  public long getMajorFaults() {
    return this.majorFaults;
  }

  @Units("bytes")
  public long getUserTime() {
    return this.userTime;
  }

  @Units("clock ticks")
  public long getKernelTime() {
    return this.kernelTime;
  }

  public int getThreads() {
    return this.threads;
  }

  @Units("bytes")
  public long getVirtualMemorySize() {
    return this.virtualMemorySize;
  }

  @Units("bytes")
  public long getResidentSetSize() {
    return this.residentSetSize;
  }

  @Units("bytes")
  public long getSoftLimit() {
    return this.softLimit;
  }

  public long getPagesSwapped() {
    return this.pagesSwapped;
  }

  @Units("clock ticks")
  public long getAggregatedBlockIoDelays() {
    return this.aggregatedBlockIoDelays;
  }

  @Units("clock ticks")
  public long getGuestTime() {
    return this.guestTime;
  }

}
