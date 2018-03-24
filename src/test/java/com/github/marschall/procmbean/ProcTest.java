package com.github.marschall.procmbean;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProcTest {

  private static Path getSampleFile(String fileName) {
    return Paths.get("src", "test", "resources", fileName);
  }

  @Test
  void getIoStatistics() {
    IoStatistics ioStatistics = Proc.getIoStatistics(getSampleFile("io-sample-input.txt"));
    assertEquals(1948, ioStatistics.getCharactersRead());
    assertEquals(1, ioStatistics.getCharactersWritten());
    assertEquals(7, ioStatistics.getReadSyscalls());
    assertEquals(2, ioStatistics.getWriteSyscalls());
    assertEquals(3, ioStatistics.getBytesRead());
    assertEquals(4, ioStatistics.getBytesWritten());
    assertEquals(5, ioStatistics.getCancelledWriteBytes());
  }

  @Test
  void getMappings() {
    List<Mapping> mappings = Proc.getMappings(getSampleFile("maps-sample-input.txt"));
    assertThat(mappings).isNotEmpty();
  }

  @Test
  void smaps() {
    Proc.smaps(getSampleFile("smaps-sample-input.txt"));
  }

  @Test
  void stat() {
    Proc.stat(getSampleFile("stat-sample-input.txt"));
  }

  @Test
  void getMemoryUsageStatistics() {
    MemoryUsageStatistics memoryUsage = Proc.getMemoryUsageStatistics(getSampleFile("statm-sample-input.txt"));
    long pageSize = 4096L;
    assertEquals(1965 * pageSize, memoryUsage.getTotalProgram());
    assertEquals(194 * pageSize, memoryUsage.getResidentSet());
    assertEquals(178 * pageSize, memoryUsage.getResidentShared());
    assertEquals(8 * pageSize, memoryUsage.getText());
    assertEquals(111 * pageSize, memoryUsage.getData());
  }

  @Test
  void status() {
    Proc.status(getSampleFile("status-sample-input.txt"));
  }

  @Test
  void getOomScore() {
    Proc.getOomScore(getSampleFile("oom_score-sample-input.txt"));
  }

}
