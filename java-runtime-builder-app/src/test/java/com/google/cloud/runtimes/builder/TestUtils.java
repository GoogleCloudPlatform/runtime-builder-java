package com.google.cloud.runtimes.builder;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {

  public static Path getTestDataDir(String name) {
    return Paths.get(System.getProperty("user.dir")).resolve("src/test/resources/testWorkspaces")
        .resolve(name);
  }

}
