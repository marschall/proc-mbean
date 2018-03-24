package com.github.marschall.procmbean;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PageSizeTest {

  @Test
  void pageSize() {
    assertEquals(4096, PageSize.pageSize());
  }

}
