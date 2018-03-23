package com.github.marschall.procmbean;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class ProcTest {

  @Test
  void testGetIoStatisticsPath() {
    IoStatistics ioStatistics = Proc.getIoStatistics(Paths.get("src", "test", "resources", "io-sample-input.txt"));
    assertEquals(1948, ioStatistics.getCharactersRead());
    assertEquals(1, ioStatistics.getCharactersWritten());
    assertEquals(7, ioStatistics.getReadSyscalls());
    assertEquals(2, ioStatistics.getWriteSyscalls());
    assertEquals(3, ioStatistics.getBytesRead());
    assertEquals(4, ioStatistics.getBytesWritten());
    assertEquals(5, ioStatistics.getCancelledWriteBytes());
  }

  @Test
  void testGetOomScorePath() {
    Proc.getOomScore(Paths.get("src", "test", "resources", "oom_score-sample-input.txt"));
  }

}
