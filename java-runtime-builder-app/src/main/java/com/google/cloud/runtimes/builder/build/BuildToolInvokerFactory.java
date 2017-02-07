package com.google.cloud.runtimes.builder.build;

import com.google.cloud.runtimes.builder.config.domain.BuildTool;

public class BuildToolInvokerFactory {

  public BuildToolInvoker get(BuildTool buildTool) {
    switch (buildTool) {
      case MAVEN:
        return new MavenInvoker();
      case GRADLE:
        return new GradleInvoker();
      default:
        throw new IllegalArgumentException(
            String.format("No invoker is available for %s", buildTool));
    }
  }
}
