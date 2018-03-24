module com.github.marschall.procmbean {

  requires transitive java.management;
  // for Unsafe.
  requires static jdk.unsupported;

  exports com.github.marschall.procmbean;

}