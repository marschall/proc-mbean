package com.github.marschall.procmbean;

import java.beans.ConstructorProperties;

public final class MemoryUsage {

  private final long totalProgram;
  private final long residentSet;
  private final long residentShared;
  private final long text;
  private final long data;

  @ConstructorProperties({"totalProgram", "residentSet", "residentShared", "text", "data"})
  public MemoryUsage(long totalProgram, long residentSet, long residentShared,
          long text, long data) {
    this.totalProgram = totalProgram;
    this.residentSet = residentSet;
    this.residentShared = residentShared;
    this.text = text;
    this.data = data;
  }

  public long getTotalProgram() {
    return this.totalProgram;
  }

  public long getResidentSet() {
    return this.residentSet;
  }

  public long getResidentShared() {
    return this.residentShared;
  }

  public long getText() {
    return this.text;
  }

  public long getData() {
    return this.data;
  }

}
