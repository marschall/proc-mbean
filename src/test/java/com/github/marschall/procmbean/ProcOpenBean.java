package com.github.marschall.procmbean;

import static com.github.marschall.procmbean.Proc.Permission.EXECUTE;
import static com.github.marschall.procmbean.Proc.Permission.PRIVATE;
import static com.github.marschall.procmbean.Proc.Permission.READ;
import static com.github.marschall.procmbean.Proc.Permission.SHARED;
import static com.github.marschall.procmbean.Proc.Permission.WRITE;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class ProcOpenBean implements DynamicMBean {

  // http://techdiary.bitourea.com/2009/06/open-mbeans-tutorial.html

  private final CompositeType mappingType;
  private final TabularType mappingTableType;

  public ProcOpenBean() throws OpenDataException {

    String[] itemNames = {"id", "size", "read", "write", "execute", "shared", "private", "pathname"};
    String[] itemDescriptions = {"id", "size", "read", "write", "execute", "shared", "private (copy on write)", "pathname"};
    OpenType<?>[] itemTypes = {SimpleType.INTEGER, SimpleType.LONG, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.STRING};
    this.mappingType = new CompositeType("mapping", "Memory Mapped Region", itemNames, itemDescriptions, itemTypes);
    this.mappingTableType = new TabularType("mappings", "List of Memory Mapped Regions", this.mappingType, new String[] {"id"});
  }

  @Override
  public Object getAttribute(String attribute)
          throws AttributeNotFoundException, MBeanException,
          ReflectionException {
    switch (attribute) {
      case "mappings":
        return this.getMappingData();
      default:
        throw new AttributeNotFoundException("Cannot find attribute" + attribute);
    }
  }

  @Override
  public void setAttribute(Attribute attribute)
          throws AttributeNotFoundException, InvalidAttributeValueException,
          MBeanException, ReflectionException {
    throw new AttributeNotFoundException("Cannot set attribute" + attribute);

  }

  @Override
  public AttributeList getAttributes(String[] attributes) {
    AttributeList attributeList = new AttributeList();
    if (attributes.length == 0) {
      return attributeList;
    }
    for (String attribute : attributes) {

      switch (attribute) {
        case "mappings":
          attributeList.add(new Attribute(attribute, this.getMappingData()));
        default:
          // ignore
      }
    }
    return attributeList;
  }

  @Override
  public AttributeList setAttributes(AttributeList attributes) {
    return new AttributeList();
  }

  @Override
  public Object invoke(String actionName, Object[] params, String[] signature)
          throws MBeanException, ReflectionException {
    NoSuchMethodException cause = new NoSuchMethodException(actionName);
    throw new ReflectionException(cause, "Cannot find the operation " + actionName);
  }

  @Override
  public MBeanInfo getMBeanInfo() {
    OpenMBeanAttributeInfoSupport[] attributes = new OpenMBeanAttributeInfoSupport[1];
    OpenMBeanConstructorInfoSupport[] constructors = new OpenMBeanConstructorInfoSupport[1];
    OpenMBeanOperationInfoSupport[] operations = new OpenMBeanOperationInfoSupport[0];
    MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[0];

    // just one attribute
    attributes[0] = new OpenMBeanAttributeInfoSupport("mappings",
            "Table of memory mappings", this.mappingTableType, true, false, false);
    // no arg constructor
    constructors[0] = new OpenMBeanConstructorInfoSupport(
            "ProcMXBean",
            "Constructs a ProcMXBean instance.",
            new OpenMBeanParameterInfoSupport[0]);

    // no operation

    // build the info

    return new OpenMBeanInfoSupport(this.getClass().getName(), "Proc - Open - MBean",
            attributes, constructors, operations, notifications);
  }


  List<Mapping> getMappings() {
    return parse(Paths.get("/proc/self/maps"));
  }

  TabularData getMappingData() {
    String[] itemNames = {"id", "size", "read", "write", "execute", "shared", "private", "pathname"};
    String[] itemDescriptions = {"id", "size", "read", "write", "execute", "shared", "private (copy on write)", "pathname"};
    OpenType<?>[] itemTypes = {SimpleType.INTEGER, SimpleType.LONG, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.STRING};
    try {
      CompositeType mappingType = new CompositeType("mapping", "Memory Mapped Region", itemNames, itemDescriptions, itemTypes);
      TabularType mappingTableType = new TabularType("mappings", "List of Memory Mapped Regions", mappingType, new String[] {"id"});

      TabularDataSupport mappingSnapshot = new TabularDataSupport(mappingTableType);

      List<Mapping> mappings = this.getMappings();
      for (int i = 0; i < mappings.size(); i++) {
        Mapping mapping = mappings.get(i);
        Object[] itemValues = {i, mapping.getSize(), mapping.isRead(), mapping.isWrite(), mapping.isExecute(), mapping.isShared(), mapping.isPrivate(), mapping.getPathname()};
        CompositeData result = new CompositeDataSupport(mappingType, itemNames, itemValues);
        mappingSnapshot.put(result);
      }
      return mappingSnapshot;
    } catch (OpenDataException e) {
      throw new RuntimeException(e);
    }
  }

  static List<Mapping> parse(Path path) {
    try (Stream<String> lines = Files.lines(path, StandardCharsets.US_ASCII)) {
      return lines
              .map(Proc::parseLine)
              .map(Proc::convertToMapping)
              .collect(toList());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  static ParsedMapping parseLine(String line) {

    String[] tokens = line.split("\\s+");
    String address = tokens[0];
    String permissions = tokens[1];
    String offset = tokens[2];
    String device = tokens[3];
    String inode = tokens[4];
    String pathname;
    if (tokens.length > 5) {
      pathname = tokens[5];
    } else {
      pathname = null;
    }

    long start = Long.parseUnsignedLong(address.substring(0, address.indexOf('-')), 16);
    long end = Long.parseUnsignedLong(address.substring(address.indexOf('-') + 1), 16);

    return new ParsedMapping(start, end,
            Permission.parse(permissions),
            Long.parseUnsignedLong(offset, 16),
            Device.parse(device),
            Integer.parseInt(inode),
            pathname);
  }

  static Mapping convertToMapping(ParsedMapping parsed) {
    return new Mapping(parsed.end - parsed.start,
            parsed.permissions.contains(READ),
            parsed.permissions.contains(WRITE),
            parsed.permissions.contains(EXECUTE),
            parsed.permissions.contains(SHARED),
            parsed.permissions.contains(PRIVATE),
            parsed.pathname);
  }

  static final class ParsedMapping {

    final long start;
    final long end;
    final Set<Permission> permissions;
    final long offset;
    final Device device;
    final long inode;
    final String pathname;

    ParsedMapping(long start, long end, Set<Permission> permissions, long offset,
            Device device, long inode, String pathname) {
      Objects.requireNonNull(permissions, "permissions");
      Objects.requireNonNull(device, "device");
      this.start = start;
      this.end = end;
      this.permissions = permissions;
      this.offset = offset;
      this.device = device;
      this.inode = inode;
      this.pathname = pathname;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(Long.toUnsignedString(this.start, 16));
      builder.append('-');
      builder.append(Long.toUnsignedString(this.end, 16));

      builder.append(' ');
      builder.append(this.permissions);

      builder.append(' ');
      builder.append(this.offset);

      builder.append(' ');
      builder.append(this.device);

      builder.append(' ');
      builder.append(this.inode);

      if (this.pathname != null) {
        builder.append(' ');
        builder.append(this.pathname);
      }

      return builder.toString();
    }

  }

  enum Permission {

    READ,
    WRITE,
    EXECUTE,
    SHARED,
    PRIVATE;

    static Set<Permission> parse(String s) {
      EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
      if (s.charAt(0) == 'r') {
        permissions.add(READ);
      }
      if (s.charAt(1) == 'w') {
        permissions.add(WRITE);
      }
      if (s.charAt(2) == 'x') {
        permissions.add(EXECUTE);
      }
      if (s.charAt(3) == 's') {
        permissions.add(SHARED);
      }
      if (s.charAt(3) == 'p') {
        permissions.add(PRIVATE);
      }
      return permissions;
    }
  }

  static final class Device {

    private final int major;
    private final int minor;

    Device(int major, int minor) {
      this.major = major;
      this.minor = minor;
    }

    static Device parse(String s) {
      int major = Integer.parseInt(s.substring(0, s.indexOf(':')));
      int minor = Integer.parseInt(s.substring(s.indexOf(':') + 1));
      return new Device(major, minor);
    }

    @Override
    public String toString() {
      return "" + this.major + ':' + this.minor;
    }

  }

}
