package com.github.marschall.procmbean;

import javax.management.openmbean.CompositeData;

public final class IoStatistics {

  private final long charactersRead;
  private final long charactersWritten;
  private final long readSyscalls;
  private final long writeSyscalls;
  private final long bytesRead;
  private final long bytesWritten;
  private final long cancelledWriteBytes;

  IoStatistics(
          long charactersRead,
          long charactersWritten,
          long readSyscalls,
          long writeSyscalls,
          long bytesRead,
          long bytesWritten,
          long cancelledWriteBytes) {
    this.charactersRead = charactersRead;
    this.charactersWritten = charactersWritten;
    this.readSyscalls = readSyscalls;
    this.writeSyscalls = writeSyscalls;
    this.bytesRead = bytesRead;
    this.bytesWritten = bytesWritten;
    this.cancelledWriteBytes = cancelledWriteBytes;
  }

  public static IoStatistics from(CompositeData compositeData) {
    return new IoStatistics(
            (Long) compositeData.get("charactersRead"),
            (Long) compositeData.get("charactersWritten"),
            (Long) compositeData.get("readSyscalls"),
            (Long) compositeData.get("writeSyscalls"),
            (Long) compositeData.get("bytesRead"),
            (Long) compositeData.get("bytesWritten"),
            (Long) compositeData.get("cancelledWriteBytes"));
  }

  public long getCharactersRead() {
    return this.charactersRead;
  }

  public long getCharactersWritten() {
    return this.charactersWritten;
  }

  public long getReadSyscalls() {
    return this.readSyscalls;
  }

  public long getWriteSyscalls() {
    return this.writeSyscalls;
  }

  public long getBytesRead() {
    return this.bytesRead;
  }

  public long getBytesWritten() {
    return this.bytesWritten;
  }

  public long getCancelledWriteBytes() {
    return this.cancelledWriteBytes;
  }

}
