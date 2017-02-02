package com.google.cloud.runtimes.builder.util;

import java.nio.file.Path;

public class FileUtil {

  public static String getFileExtension(Path file) {
    String name = file.getFileName().toString();
    return name.substring(name.lastIndexOf(".") + 1);
  }

}
