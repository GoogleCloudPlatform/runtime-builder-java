package com.google.cloud.runtimes.builder.build;

import com.google.cloud.runtimes.builder.config.domain.BuildTool;

/**
 * Factory class for {@link BuildToolInvoker}.
 */
public class BuildToolInvokerFactory {

  /**
   * Returns the suitable {@link BuildToolInvoker} for the given {@link BuildTool}.
   */
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
