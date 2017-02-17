package com.google.cloud.runtimes.builder.config.domain;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum that represents a Java build tool.
 */
public enum BuildTool {
  MAVEN("pom.xml", "target"),
  GRADLE("build.gradle", "build/libs");

  private static final Map<String, BuildTool> BUILD_FILENAME_TO_BUILD_TOOL = new HashMap<>();
  static {
    BUILD_FILENAME_TO_BUILD_TOOL.put("pom.xml", MAVEN);
    BUILD_FILENAME_TO_BUILD_TOOL.put("build.gradle", GRADLE);
  }

  private final String defaultOutputPath;
  private final String buildFileName;

  BuildTool(String buildFileName, String defaultOutputPath) {
    this.defaultOutputPath = defaultOutputPath;
    this.buildFileName = buildFileName;
  }

  /**
   * Returns the default path for build output for this build tool.
   */
  public String getDefaultOutputPath() {
    return this.defaultOutputPath;
  }

  /**
   * Returns the name of this build tool's build file.
   */
  public String getBuildFileName() {
    return this.buildFileName;
  }

  /**
   * Looks up the {@link BuildTool} associated with the given build file.
   */
  public static BuildTool getForBuildFile(Path buildFilePath) {
    String fileName = buildFilePath.getFileName().toString();
    if (BUILD_FILENAME_TO_BUILD_TOOL.containsKey(fileName)) {
      return BUILD_FILENAME_TO_BUILD_TOOL.get(fileName);
    } else {
      throw new IllegalArgumentException(
          String.format("No build tool found for build file named %s", fileName));
    }
  }

  public static boolean isABuildFile(Path path) {
    return BUILD_FILENAME_TO_BUILD_TOOL.containsKey(path.getFileName().toString());
  }
}
