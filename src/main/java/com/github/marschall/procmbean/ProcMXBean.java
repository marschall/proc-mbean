package com.github.marschall.procmbean;

import java.util.List;

public interface ProcMXBean {

  // grep -i pagesize /proc/self/smaps

  String smaps();

  ProcessStat getStat();

  MemoryUsageStatistics getMemoryUsageStatistics();

  String status();

  List<Mapping> getMappings();

  int getOomScore();

  IoStatistics getIoStatistics();

  String mappingsString(char separator);

}
