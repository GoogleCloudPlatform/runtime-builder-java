package com.google.cloud.runtimes.builder.build;

import com.google.cloud.runtimes.builder.workspace.ProjectType;

public class BuildToolInvokerFactory {

  public BuildToolInvoker get(ProjectType projectType) {
    switch (projectType) {
      case MAVEN:
        return new MavenInvoker();
      case GRADLE:
        return new GradleInvoker();
      default:
        throw new IllegalArgumentException(
            String.format("No invoker is available for %s", projectType));
    }
  }
}
