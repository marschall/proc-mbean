package com.github.marschall.procmbean;

import java.io.IOException;

import javax.management.JMException;

public class InstallBean {

  public static void main(String[] args) throws JMException, IOException {
    Proc.install();
    System.in.read();
  }

}
