package com.google.cloud.runtimes.builder.config.domain;

import com.google.cloud.runtimes.builder.buildsteps.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.gradle.GradleBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.maven.MavenBuildStep;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Enum that represents a Java build tool.
 */
public enum BuildTool {
  MAVEN("pom.xml", "target", MavenBuildStep::new),
  GRADLE("build.gradle", "build/libs", GradleBuildStep::new);

  private static final Map<String, BuildTool> BUILD_FILENAME_TO_BUILD_TOOL = new HashMap<>();
  static {
    BUILD_FILENAME_TO_BUILD_TOOL.put("pom.xml", MAVEN);
    BUILD_FILENAME_TO_BUILD_TOOL.put("build.gradle", GRADLE);
  }

  private final String defaultOutputPath;
  private final String buildFileName;
  private final Function<List<String>, BuildStep> buildStepConstructor;

  BuildTool(String buildFileName, String defaultOutputPath, Function<List<String>,
      BuildStep> buildStepConstructor) {
    this.defaultOutputPath = defaultOutputPath;
    this.buildFileName = buildFileName;
    this.buildStepConstructor = buildStepConstructor;
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
   * Returns a build step for this build tool.
   */
  public BuildStep getBuildStep(List<String> buildStepArgs) {
    return this.buildStepConstructor.apply(buildStepArgs);
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
    return BUILD_FILENAME_TO_BUILD_TOOL.containsKey(path.getFileName());
  }
}
