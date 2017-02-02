package com.google.cloud.runtimes.builder.workspace;

public enum ProjectType {
  MAVEN("maven", "pom.xml", "target"), GRADLE("gradle", "build.gradle", "build/libs"),
  NONE("none", null, "");

  private final String buildToolName;
  private final String defaultOutputPath;
  private final String buildFileName;

  ProjectType(String buildToolName, String buildFileName, String defaultOutputPath) {
    this.buildToolName = buildToolName;
    this.defaultOutputPath = defaultOutputPath;
    this.buildFileName = buildFileName;
  }

  public String getDefaultOutputPath() {
    return this.defaultOutputPath;
  }

  public String getBuildFileName() {
    return this.buildFileName;
  }

  public static ProjectType getForBuildToolName(String name) {
    // TODO refactor
    if (name.equals("maven")) {
      return ProjectType.MAVEN;
    } else if (name.equals("gradle")) {
      return ProjectType.GRADLE;
    } else {
      return null;
    }
  }

  public static ProjectType getForBuildFileName(String fileName) {
    // TODO refactor
    if (fileName.equals("pom.xml")) {
      return ProjectType.MAVEN;
    } else if (fileName.equals("build.gradle")) {
      return ProjectType.GRADLE;
    } else {
      return null;
    }
  }

}
