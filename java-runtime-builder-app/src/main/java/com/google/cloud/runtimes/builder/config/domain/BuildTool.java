package com.google.cloud.runtimes.builder.config.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import java.nio.file.Path;

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
  public String getBuildToolName() {
    return buildToolName;
  }

  public String getDefaultOutputPath() {
    return this.defaultOutputPath;
  }

  public String getBuildFileName() {
    return this.buildFileName;
  }

  public static BuildTool getForName(String name) {
    // TODO refactor
    if (name.equals("maven")) {
      return BuildTool.MAVEN;
    } else if (name.equals("gradle")) {
      return BuildTool.GRADLE;
    } else {
      return null;
    }
  }

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
