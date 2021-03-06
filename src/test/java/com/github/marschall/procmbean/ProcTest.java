package com.github.marschall.procmbean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

class ProcTest {

  private static Path getSampleFile(String fileName) {
    return Paths.get("src", "test", "resources", fileName);
  }

  @Test
  void getIoStatistics() {
    IoStatistics ioStatistics = Proc.getIoStatistics(getSampleFile("io-sample-input.txt"));
    assertEquals(469225655L, ioStatistics.getCharactersRead());
    assertEquals(414040098L, ioStatistics.getCharactersWritten());
    assertEquals(147430L, ioStatistics.getReadSyscalls());
    assertEquals(389953L, ioStatistics.getWriteSyscalls());
    assertEquals(192565248L, ioStatistics.getBytesRead());
    assertEquals(8749056L, ioStatistics.getBytesWritten());
    assertEquals(344064L, ioStatistics.getCancelledWriteBytes());
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
  void getStat() {
    ProcessStat stat = Proc.getStat(getSampleFile("stat-sample-input.txt"));
    long pageSize = 4096;
    assertEquals(3335, stat.getPid());
    assertEquals('S', stat.getState());
    assertEquals(1476957L, stat.getMinorFaults());
    assertEquals(2643L, stat.getMajorFaults());
    assertEquals(57048L, stat.getUserTime());
    assertEquals(1940L, stat.getKernelTime());
    assertEquals(48, stat.getThreads());
    assertEquals(8370806784L, stat.getVirtualMemorySize());
    assertEquals(226096L * pageSize, stat.getResidentSetSize());
    assertEquals(Long.parseUnsignedLong("18446744073709551615"), stat.getSoftLimit());
    assertEquals(0L, stat.getPagesSwapped());
    assertEquals(0L, stat.getAggregatedBlockIoDelays());
    assertEquals(0L, stat.getGuestTime());
  }

  @Test
  void getMemoryUsageStatistics() {
    MemoryUsageStatistics memoryUsage = Proc.getMemoryUsageStatistics(getSampleFile("statm-sample-input.txt"));
    long pageSize = 4096L;
    assertEquals(2043654 * pageSize, memoryUsage.getTotalProgram());
    assertEquals(226084 * pageSize, memoryUsage.getResidentSet());
    assertEquals(21643 * pageSize, memoryUsage.getResidentShared());
    assertEquals(1 * pageSize, memoryUsage.getText());
    assertEquals(329081 * pageSize, memoryUsage.getData());
  }

  @Test
  void getStatus() {
    ProcessStatus status = Proc.getStatus(getSampleFile("status-sample-input.txt"));
    assertEquals("S (sleeping)", status.getState());
    assertEquals(256, status.getFileDescriptorSlotsAllocated());
    assertEquals(8252972L * 1024, status.getVirtualMemoryPeak());
    assertEquals(8174660L * 1024, status.getVirtualMemory());
    assertEquals(0L * 1024, status.getLockedMemory());
    assertEquals(0L * 1024, status.getPinnedMemory());
    assertEquals(1003180L * 1024, status.getResidentSetPreak());
    assertEquals(921468L * 1024, status.getResidentSet());
    assertEquals(834852L * 1024, status.getResidentSetAnonymous());
    assertEquals(83204L * 1024, status.getResidentSetFile());
    assertEquals(3412L * 1024, status.getResidentSetShared());
    assertEquals(1318472L * 1024, status.getData());
    assertEquals(136L * 1024, status.getStack());
    assertEquals(4L * 1024, status.getText());
    assertEquals(170400L * 1024, status.getSharedLibraryCode());
    assertEquals(0L * 1024, status.getSwapped());
    assertEquals(49L, status.getThreads());
    assertEquals(4L, status.getContextSwitchesVoluntary());
    assertEquals(1L, status.getContextSwitchesInvoluntary());
  }

  @Test
  void getOomScore() {
    int oomScore = Proc.getOomScore(getSampleFile("oom_score-sample-input.txt"));
    assertEquals(13L, oomScore);
  }

  @Test
  void parseMemory() {
    assertEquals(0L, Proc.parseMemory("0 kB"));
    assertEquals(1003180L * 1024L, Proc.parseMemory("1003180 kB"));
  }

}
