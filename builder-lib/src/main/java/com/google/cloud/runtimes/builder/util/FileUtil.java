package com.google.cloud.runtimes.builder.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FileUtil {

  public static List<Path> findMatchingFilesInDir(Path dir, Predicate<File> predicate)
      throws IOException {
    File dirAsFile = dir.toFile().getCanonicalFile();
    if (!dirAsFile.exists() || !dirAsFile.isDirectory()) {
      throw new IllegalArgumentException(
          String.format("%s is not a valid directory.", dirAsFile.toString()));
    }

    List<Path> matches = new ArrayList<>();
    File[] files = dirAsFile.listFiles();
    for (File file : files) {
      if (predicate.test(file)) {
        matches.add(file.toPath());
      }
    }
    return matches;
  }

  public static String getFileExtension(Path file) {
    String name = file.getFileName().toString();
    return name.substring(name.lastIndexOf(".") + 1);
  }

}
