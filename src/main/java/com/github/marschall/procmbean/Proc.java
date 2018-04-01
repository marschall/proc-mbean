package com.github.marschall.procmbean;

import static com.github.marschall.procmbean.PageSize.pageSize;
import static com.github.marschall.procmbean.Proc.Permission.EXECUTE;
import static com.github.marschall.procmbean.Proc.Permission.PRIVATE;
import static com.github.marschall.procmbean.Proc.Permission.READ;
import static com.github.marschall.procmbean.Proc.Permission.SHARED;
import static com.github.marschall.procmbean.Proc.Permission.WRITE;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class Proc implements ProcMXBean {

  private static final String OBJECT_NAME = "com.github.marschall.procmbean:type=Proc";

  private final Path procSelf;

  public Proc() {
    this.procSelf = Paths.get("/proc/self");
  }

  @Override
  public IoStatistics getIoStatistics() {
    return getIoStatistics(this.procSelf.resolve("io"));
  }

  static IoStatistics getIoStatistics(Path path) {
    try (InputStream input = Files.newInputStream(path);
         Reader reader = new InputStreamReader(input, StandardCharsets.US_ASCII);
         BufferedReader bufferedReader = new BufferedReader(reader, 32)) {

      long charactersRead = 0L;
      long charactersWritten = 0L;
      long readSyscalls = 0L;
      long writeSyscalls = 0L;
      long bytesRead = 0L;
      long bytesWritten = 0L;
      long cancelledWriteBytes = 0L;
      String line = bufferedReader.readLine();
      while (line != null) {
        // TODO more robust
        String key = line.substring(0, line.indexOf(':'));
        long value = Long.parseUnsignedLong(line.substring(line.indexOf(':') + 2));
        switch (key) {
          case "rchar":
            charactersRead = value;
            break;
          case "wchar":
            charactersWritten = value;
            break;
          case "syscr":
            readSyscalls = value;
            break;
          case "syscw":
            writeSyscalls = value;
            break;
          case "read_bytes":
            bytesRead = value;
            break;
          case "write_bytes":
            bytesWritten = value;
            break;
          case "cancelled_write_bytes":
            cancelledWriteBytes = value;
            break;
          default:
            // ignore
            break;
        }

        line = bufferedReader.readLine();
      }
      return new IoStatistics(charactersRead, charactersWritten, readSyscalls, writeSyscalls, bytesRead, bytesWritten, cancelledWriteBytes);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public int getOomScore() {
    return getOomScore(this.procSelf.resolve("oom_score"));
  }

  static int getOomScore(Path path) {
    try (InputStream input = Files.newInputStream(path);
         Reader reader = new InputStreamReader(input, StandardCharsets.US_ASCII);
         BufferedReader bufferedReader = new BufferedReader(reader, 9)) {
      String line = bufferedReader.readLine();
      return Integer.parseInt(line);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public List<Mapping> getMappings() {
    return getMappings(this.procSelf.resolve("maps"));
  }

  @Override
  public String mappingsString(char separator) {
    // TODO test
    StringBuilder buffer = new StringBuilder();
    buffer.append("size");
    buffer.append(separator);
    buffer.append("read");
    buffer.append(separator);
    buffer.append("write");
    buffer.append(separator);
    buffer.append("execute");
    buffer.append(separator);
    buffer.append("shared");
    buffer.append(separator);
    buffer.append("private");
    buffer.append(separator);
    buffer.append("pathname\n");

    // TODO avoid building list
    for (Mapping mapping : this.getMappings()) {
      String pathname = mapping.getPathname();
      if (pathname != null) {
        buffer.append(Long.toUnsignedString(mapping.getSize()));
        buffer.append(separator);
        buffer.append(Boolean.toString(mapping.isRead()));
        buffer.append(separator);
        buffer.append(Boolean.toString(mapping.isWrite()));
        buffer.append(separator);
        buffer.append(Boolean.toString(mapping.isExecute()));
        buffer.append(separator);
        buffer.append(Boolean.toString(mapping.isShared()));
        buffer.append(separator);
        buffer.append(Boolean.toString(mapping.isPrivate()));
        buffer.append(separator);
        buffer.append(pathname);
        buffer.append('\n');
      }
    }
    return buffer.toString();
  }

  static List<Mapping> getMappings(Path path) {
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

  @Override
  public String getSmaps() {
    return smaps(this.procSelf.resolve("maps"));
  }

  static String smaps(Path path) {
    // TODO implement
    return "smaps";
  }

  @Override
  public ProcessStat getStat() {
    return getStat(this.procSelf.resolve("stat"));
  }

  static ProcessStat getStat(Path path) {

    try (InputStream input = Files.newInputStream(path);
         Reader reader = new InputStreamReader(input, StandardCharsets.US_ASCII);
         BufferedReader bufferedReader = new BufferedReader(reader, 1024)) {

      String line = bufferedReader.readLine();
      String[] elements = line.split(" ");

      int pageSize = pageSize();
      int pid = Integer.parseUnsignedInt(elements[0]);
      // TODO handle comm with spaces
      char state = elements[2].charAt(0);
      long minorFaults = Long.parseUnsignedLong(elements[9]);
      long majorFaults = Long.parseUnsignedLong(elements[10]);
      long userTime = Long.parseUnsignedLong(elements[13]);
      long kernelTime = Long.parseUnsignedLong(elements[14]);
      int threads = Integer.parseUnsignedInt(elements[19]);
      long virtualMemorySize = Long.parseUnsignedLong(elements[22]);
      long residentSetSize = Long.parseUnsignedLong(elements[23]) * pageSize;
      long softLimit = Long.parseUnsignedLong(elements[24]);
      long pagesSwapped = Long.parseUnsignedLong(elements[35]);
      long aggregatedBlockIoDelays = Long.parseUnsignedLong(elements[41]);
      long guestTime = Long.parseUnsignedLong(elements[42]);
      return new ProcessStat(pid, state, minorFaults, majorFaults, userTime, kernelTime,
              threads, virtualMemorySize, residentSetSize, softLimit, pagesSwapped,
              aggregatedBlockIoDelays, guestTime);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public MemoryUsageStatistics getMemoryUsageStatistics() {
    return getMemoryUsageStatistics(this.procSelf.resolve("statm"));
  }

  static MemoryUsageStatistics getMemoryUsageStatistics(Path path) {

    try (InputStream input = Files.newInputStream(path);
         Reader reader = new InputStreamReader(input, StandardCharsets.US_ASCII);
         BufferedReader bufferedReader = new BufferedReader(reader, 64)) {

      String line = bufferedReader.readLine();
      String[] elements = line.split(" ");

      int pageSize = pageSize();
      long totalProgram = Long.parseUnsignedLong(elements[0]) * pageSize;
      long residentSet = Long.parseUnsignedLong(elements[1]) * pageSize;
      long residentShared = Long.parseUnsignedLong(elements[2]) * pageSize;
      long text = Long.parseUnsignedLong(elements[3]) * pageSize;
      long data = Long.parseUnsignedLong(elements[5]) * pageSize;
      return new MemoryUsageStatistics(totalProgram, residentSet, residentShared, text, data);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public ProcessStatus getStatus() {
    return getStatus(this.procSelf.resolve("status"));
  }

  static ProcessStatus getStatus(Path path) {
    try (InputStream input = Files.newInputStream(path);
         Reader reader = new InputStreamReader(input, StandardCharsets.US_ASCII);
         BufferedReader bufferedReader = new BufferedReader(reader, 1024)) {

      String state = "";
      int fileDescriptorSlotsAllocated = 0;
      long virtualMemoryPeak = 0L;
      long virtualMemory = 0L;
      long lockedMemory = 0L;
      long pinnedMemory = 0L;
      long residentSet = 0L;
      long residentSetPreak = 0L;
      long residentSetAnonymous = 0L;
      long residentSetFile = 0L;
      long residentSetShared = 0L;
      long data = 0L;
      long text = 0L;
      long stack = 0L;
      long sharedLibraryCode = 0L;
      long swapped = 0L;
      int threads = 0;
      long contextSwitchesInvoluntary = 0L;
      long contextSwitchesVoluntary = 0L;
      String line = bufferedReader.readLine();
      while (line != null) {
        // TODO more robust
        String key = line.substring(0, line.indexOf(':'));
        String value = line.substring(line.indexOf(':') + 2);
        switch (key) {
          case "State":
            state = value;
            break;
          case "FDSize":
            fileDescriptorSlotsAllocated = Integer.parseUnsignedInt(value);
            break;
          case "VmPeak":
            virtualMemoryPeak = parseMemory(value);
            break;
          case "VmSize":
            virtualMemory = parseMemory(value);
            break;
          case "VmLck":
            lockedMemory = parseMemory(value);
            break;
          case "VmPin":
            pinnedMemory = parseMemory(value);
            break;
          case "VmHWM":
            residentSetPreak = parseMemory(value);
            break;
          case "VmRSS":
            residentSet = parseMemory(value);
            break;
          case "RssAnon":
            residentSetAnonymous = parseMemory(value);
            break;
          case "RssFile":
            residentSetFile = parseMemory(value);
            break;
          case "RssShmem":
            residentSetShared = parseMemory(value);
            break;
          case "VmData":
            data = parseMemory(value);
            break;
          case "VmStk":
            stack = parseMemory(value);
            break;
          case "VmExe":
            text = parseMemory(value);
            break;
          case "VmLib":
            sharedLibraryCode = parseMemory(value);
            break;
          case "VmSwap":
            swapped = parseMemory(value);
            break;
          case "Threads":
            threads = Integer.parseUnsignedInt(value);
            break;
          case "voluntary_ctxt_switches":
            contextSwitchesVoluntary = Long.parseUnsignedLong(value);
            break;
          case "nonvoluntary_ctxt_switches":
            contextSwitchesInvoluntary = Long.parseUnsignedLong(value);
            break;
          default:
            // ignore
            break;
        }

        line = bufferedReader.readLine();
      }
      return new ProcessStatus(state, fileDescriptorSlotsAllocated, virtualMemoryPeak, virtualMemory, lockedMemory, pinnedMemory,
              residentSet, residentSetPreak, residentSetAnonymous, residentSetFile, residentSetShared, data, text, stack,
              sharedLibraryCode, swapped, threads, contextSwitchesInvoluntary, contextSwitchesVoluntary);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  static long parseMemory(String s) {
    if (s.equals("0 kB")) {
      return 0L;
    }
    int start = 0;
    while ((s.charAt(start) == ' ') || (s.charAt(start) == '\t')) {
      start += 1;
    }
    long value = Long.parseUnsignedLong(s.substring(start, s.indexOf(' ', start + 1)));
    return value * getMultiplier(s.substring(s.lastIndexOf(' ') + 1));
  }

  public static void install() throws JMException {
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    ObjectName mxBeanName = new ObjectName(OBJECT_NAME);
    Proc mxBean = new Proc();
    server.registerMBean(mxBean, mxBeanName);
  }

  public static void uninstall() throws JMException {
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    ObjectName mxBeanName = new ObjectName(OBJECT_NAME);
    server.unregisterMBean(mxBeanName);
  }

  static int getMultiplier(String unit) {
    if ((unit == null) || unit.isEmpty()) {
      return 1;
    }
    if (unit.equalsIgnoreCase("kb")) {
      return 1024;
    }
    if (unit.equalsIgnoreCase("mb")) {
      return 1024 * 1024;
    }
    if (unit.equalsIgnoreCase("gb")) {
      return 1024 * 1024 * 1024;
    }
    throw new IllegalArgumentException("unknown unit:" + unit);
  }

}
