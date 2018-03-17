package com.github.marschall.procmbean;

import java.util.List;

public interface ProcMXBean {

  List<Mapping> getMappings();

  String mappingsString();

}
