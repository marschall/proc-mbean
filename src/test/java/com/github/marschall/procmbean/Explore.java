package com.github.marschall.procmbean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

final class Explore {

  @Test
  void explore() throws IOException {
    try (Stream<String> lines = Files.lines(Paths.get("/proc/self/maps"))) {
      lines.forEach(line -> {
//        List<String> tokens = new ArrayList<>(5);
//        Scanner scanner = new Scanner(line).useDelimiter("\\s+");
//        for (String token : tokens) {
//          tokens.add(token);
//        }
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

        Mapping mapping = new Mapping(start, end,
                Permission.parse(permissions),
                Long.parseUnsignedLong(offset, 16),
                Device.parse(device),
                Integer.parseInt(inode),
                pathname);


        System.out.println(mapping);
      });
    }
  }

  static final class Mapping {

    private final long start;
    private final long end;
    private final Set<Permission> permissions;
    private final long offset;
    private final Device device;
    private final long inode;
    private final String pathname;

    Mapping(long start, long end, Set<Permission> permissions, long offset,
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
