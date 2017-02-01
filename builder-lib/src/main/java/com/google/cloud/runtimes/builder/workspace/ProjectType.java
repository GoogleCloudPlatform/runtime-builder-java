package com.google.cloud.runtimes.builder.workspace;

public enum ProjectType {
  MAVEN("target"), GRADLE("build"), NONE("");

  private final String outputPath;

  ProjectType(String outputPath) {
    this.outputPath = outputPath;
  }

  public String getOutputPath() {
    return this.outputPath;
  }

}
