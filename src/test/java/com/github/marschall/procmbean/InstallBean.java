package com.github.marschall.procmbean;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class InstallBean {

  public static void main(String[] args) throws JMException, IOException {
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    ObjectName mxBeanName = new ObjectName("com.github.marschall.procmbean:type=Proc");

    Proc mxBean = new Proc();

    server.registerMBean(mxBean, mxBeanName);

    System.in.read();
  }

}
