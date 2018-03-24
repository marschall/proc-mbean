package com.github.marschall.procmbean;

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

  public long getUserTime() {
    return this.userTime;
  }

  public long getKernelTime() {
    return this.kernelTime;
  }

  public int getThreads() {
    return this.threads;
  }

  public long getVirtualMemorySize() {
    return this.virtualMemorySize;
  }

  public long getResidentSetSize() {
    return this.residentSetSize;
  }

  public long getSoftLimit() {
    return this.softLimit;
  }

  public long getPagesSwapped() {
    return this.pagesSwapped;
  }

  public long getAggregatedBlockIoDelays() {
    return this.aggregatedBlockIoDelays;
  }

  public long getGuestTime() {
    return this.guestTime;
  }

}
