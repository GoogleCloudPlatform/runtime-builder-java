/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Build step that invokes gradle.
 */
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

      metadata.put(BuildStepMetadataConstants.BUILD_ARTIFACT_PATH, "build/libs");

    } catch (IOException | InterruptedException e) {
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
      throw new IllegalStateException("$GRADLE_HOME must be set.");
    }
    Path systemGradle = Paths.get(gradleHome).resolve("bin").resolve("gradle");
    if (Files.isExecutable(systemGradle)) {
      return systemGradle.toString();
    }

    throw new IllegalStateException(
        String.format("The file at %s is not a valid gradle executable", systemGradle.toString()));
  }
}
