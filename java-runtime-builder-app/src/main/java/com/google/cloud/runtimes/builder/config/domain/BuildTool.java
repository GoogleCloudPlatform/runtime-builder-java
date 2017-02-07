package com.google.cloud.runtimes.builder.config.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.nio.file.Path;

/**
 * Enum that represents a Java build tool.
 */
public enum BuildTool {
  MAVEN("maven", "pom.xml", "target"), GRADLE("gradle", "build.gradle", "build/libs");

  private final String buildToolName;
  private final String defaultOutputPath;
  private final String buildFileName;

  BuildTool(String buildToolName, String buildFileName, String defaultOutputPath) {
    this.buildToolName = buildToolName;
    this.defaultOutputPath = defaultOutputPath;
    this.buildFileName = buildFileName;
  }

  @JsonValue
  String getBuildToolName() {
    return buildToolName;
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
    // TODO refactor
    String fileName = buildFilePath.getFileName().toString();
    if (fileName.equals("pom.xml")) {
      return BuildTool.MAVEN;
    } else if (fileName.equals("build.gradle")) {
      return BuildTool.GRADLE;
    } else {
      return null;
    }
  }

}
