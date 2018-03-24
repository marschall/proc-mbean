package com.github.marschall.procmbean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class PageSize {

  private static final int PAGE_SIZE;

  static {
    int pageSize = 4096;
    try {
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      Field singleoneInstanceField = unsafeClass.getDeclaredField("theUnsafe");
      if (!singleoneInstanceField.isAccessible()) {
        singleoneInstanceField.setAccessible(true);
      }
      Object unsafe = singleoneInstanceField.get(null);

      Method pageSizeMethod = unsafeClass.getDeclaredMethod("pageSize");
      pageSize = (int) pageSizeMethod.invoke(unsafe);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("could not get page size", e);
    }
    PAGE_SIZE = pageSize;
  }

  static int pageSize() {
    return PAGE_SIZE;
  }

}
