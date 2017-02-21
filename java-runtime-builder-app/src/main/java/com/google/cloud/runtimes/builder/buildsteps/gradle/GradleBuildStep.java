package com.google.cloud.runtimes.builder.buildsteps.gradle;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepMetadataConstants;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class GradleBuildStep extends BuildStep {

  private final Logger logger = LoggerFactory.getLogger(GradleBuildStep.class);

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {
    try {
      new ProcessBuilder()
          .command(getGradleExecutable(directory), "build")
          .directory(directory.toFile())
          .inheritIO()
          .start().waitFor();

      // TODO look for build output overrides?
      metadata.put(BuildStepMetadataConstants.BUILD_ARTIFACT_PATH, "build/libs");

    } catch (IOException | InterruptedException e) {
      // TODO handle interrupt differently?
      throw new BuildStepException(e);
    }
  }

  private String getGradleExecutable(Path directory) {
    Path wrapperPath = directory.resolve("gradlew");
    if (Files.isExecutable(wrapperPath)) {
      logger.info("Gradle wrapper discovered at {}. Using wrapper instead of system gradle.",
          wrapperPath.toString());
      return wrapperPath.toString();
    }

    String gradleHome = System.getenv("GRADLE_HOME");
    if (Strings.isNullOrEmpty(gradleHome)) {
      throw new IllegalStateException("$GRADLE_HOME must be set");
    }
    Path systemGradle = Paths.get(gradleHome).resolve("bin").resolve("gradle");
    if (Files.isExecutable(systemGradle)) {
      return systemGradle.toString();
    }

    throw new IllegalStateException(
        String.format("The file at %s is not a valid gradle executable", systemGradle.toString()));
  }
}
