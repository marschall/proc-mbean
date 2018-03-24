module com.github.marschall.procmbean {

  requires java.management;
  // for Unsafe.
  requires static jdk.unsupported;

  exports com.github.marschall.procmbean;

}