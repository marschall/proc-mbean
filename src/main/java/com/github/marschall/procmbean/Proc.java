package com.github.marschall.procmbean;

import static com.github.marschall.procmbean.Proc.Permission.EXECUTE;
import static com.github.marschall.procmbean.Proc.Permission.PRIVATE;
import static com.github.marschall.procmbean.Proc.Permission.READ;
import static com.github.marschall.procmbean.Proc.Permission.SHARED;
import static com.github.marschall.procmbean.Proc.Permission.WRITE;
import static java.util.stream.Collectors.toList;
import static com.github.marschall.procmbean.PageSize.pageSize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

public class Proc implements ProcMXBean {

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
  public String smaps() {
    return smaps(this.procSelf.resolve("maps"));
  }

  static String smaps(Path path) {
    return "smaps";
  }

  @Override
  public String stat() {
    return stat(this.procSelf.resolve("stat"));
  }

  static String stat(Path path) {
    return "stat";
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
  public String status() {
    return status(this.procSelf.resolve("status"));
  }

  static String status(Path path) {
    return "status";
  }

}
