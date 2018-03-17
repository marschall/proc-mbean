package com.github.marschall.procmbean;

import java.beans.ConstructorProperties;

public class Mapping {

  private final long size;
  private final boolean read;
  private final boolean write;
  private final boolean execute;
  private final boolean shared;
  private final boolean _private;
  private final String pathname;

  @ConstructorProperties({"size", "read", "write", "execute", "shared", "private", "pathname"})
  public Mapping(long size, boolean read, boolean write, boolean execute,
          boolean shared, boolean prvate, String pathname) {
    this.size = size;
    this.read = read;
    this.write = write;
    this.execute = execute;
    this.shared = shared;
    this._private = prvate;
    this.pathname = pathname;
  }

  public long getSize() {
    return this.size;
  }

  public boolean isRead() {
    return this.read;
  }

  public boolean isWrite() {
    return this.write;
  }

  public boolean isExecute() {
    return this.execute;
  }

  public boolean isShared() {
    return this.shared;
  }

  public boolean isPrivate() {
    return this._private;
  }

  public String getPathname() {
    return this.pathname;
  }

}
