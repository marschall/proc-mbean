package com.github.marschall.procmbean;

import javax.management.openmbean.CompositeData;

public final class ProcessStatus {

  private final String state;
  private final int fileDescriptorSlotsAllocated;
  private final long virtualMemoryPeak;
  private final long virtualMemory;
  private final long lockedMemory;
  private final long pinnedMemory;
  private final long residentSet;
  private final long residentSetPreak;
  private final long residentSetAnonymous;
  private final long residentSetFile;
  private final long residentSetShared;
  private final long data;
  private final long text;
  private final long stack;
  private final long sharedLibraryCode;
  private final long swapped;
  private final int threads;
  private final long contextSwitchesInvoluntary;
  private final long contextSwitchesVoluntary;

  ProcessStatus(
          String state,
          int fileDescriptorSlotsAllocated,
          long virtualMemoryPeak,
          long virtualMemory,
          long lockedMemory,
          long pinnedMemory,
          long residentSet,
          long residentSetPreak,
          long residentSetAnonymous,
          long residentSetFile,
          long residentSetShared,
          long data,
          long text,
          long stack,
          long sharedLibraryCode,
          long swapped,
          int threads,
          long contextSwitchesInvoluntary,
          long contextSwitchesVoluntary) {
    this.state = state;
    this.fileDescriptorSlotsAllocated = fileDescriptorSlotsAllocated;
    this.virtualMemoryPeak = virtualMemoryPeak;
    this.virtualMemory = virtualMemory;
    this.lockedMemory = lockedMemory;
    this.pinnedMemory = pinnedMemory;
    this.residentSet = residentSet;
    this.residentSetPreak = residentSetPreak;
    this.residentSetAnonymous = residentSetAnonymous;
    this.residentSetFile = residentSetFile;
    this.residentSetShared = residentSetShared;
    this.data = data;
    this.text = text;
    this.stack = stack;
    this.sharedLibraryCode = sharedLibraryCode;
    this.swapped = swapped;
    this.threads = threads;
    this.contextSwitchesInvoluntary = contextSwitchesInvoluntary;
    this.contextSwitchesVoluntary = contextSwitchesVoluntary;
  }

  public static ProcessStatus from(CompositeData compositeData) {
    return new ProcessStatus(
            (String) compositeData.get("state"),
            (Integer) compositeData.get("fileDescriptorSlotsAllocated"),
            (Long) compositeData.get("virtualMemoryPeak"),
            (Long) compositeData.get("virtualMemory"),
            (Long) compositeData.get("lockedMemory"),
            (Long) compositeData.get("pinnedMemory"),
            (Long) compositeData.get("residentSet"),
            (Long) compositeData.get("residentSetPreak"),
            (Long) compositeData.get("residentSetAnonymous"),
            (Long) compositeData.get("residentSetFile"),
            (Long) compositeData.get("residentSetShared"),
            (Long) compositeData.get("data"),
            (Long) compositeData.get("text"),
            (Long) compositeData.get("stack"),
            (Long) compositeData.get("sharedLibraryCode"),
            (Long) compositeData.get("swapped"),
            (Integer) compositeData.get("threads"),
            (Long) compositeData.get("contextSwitchesInvoluntary"),
            (Long) compositeData.get("contextSwitchesVoluntary"));
  }

  public String getState() {
    return this.state;
  }

  public int getFileDescriptorSlotsAllocated() {
    return this.fileDescriptorSlotsAllocated;
  }

  public long getVirtualMemoryPeak() {
    return this.virtualMemoryPeak;
  }

  public long getVirtualMemory() {
    return this.virtualMemory;
  }

  public long getLockedMemory() {
    return this.lockedMemory;
  }

  public long getPinnedMemory() {
    return this.pinnedMemory;
  }

  public long getResidentSet() {
    return this.residentSet;
  }

  public long getResidentSetPreak() {
    return this.residentSetPreak;
  }

  public long getResidentSetAnonymous() {
    return this.residentSetAnonymous;
  }

  public long getResidentSetFile() {
    return this.residentSetFile;
  }

  public long getResidentSetShared() {
    return this.residentSetShared;
  }

  public long getData() {
    return this.data;
  }

  public long getText() {
    return this.text;
  }

  public long getStack() {
    return this.stack;
  }

  public long getSharedLibraryCode() {
    return this.sharedLibraryCode;
  }

  public long getSwapped() {
    return this.swapped;
  }

  public int getThreads() {
    return this.threads;
  }

  public long getContextSwitchesInvoluntary() {
    return this.contextSwitchesInvoluntary;
  }

  public long getContextSwitchesVoluntary() {
    return this.contextSwitchesVoluntary;
  }

}
