package com.github.marschall.procmbean;

import java.util.List;

public interface ProcMXBean {

  String getSmaps();

  ProcessStat getStat();

  MemoryUsageStatistics getMemoryUsageStatistics();

  ProcessStatus getStatus();

  List<Mapping> getMappings();

  int getOomScore();

  IoStatistics getIoStatistics();

  String mappingsString(char separator);

}
