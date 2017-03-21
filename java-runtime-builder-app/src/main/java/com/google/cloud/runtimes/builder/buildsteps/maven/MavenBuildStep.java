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

package com.google.cloud.runtimes.builder.buildsteps.maven;

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

public class MavenBuildStep extends BuildStep {

  private static final Logger logger = LoggerFactory.getLogger(MavenBuildStep.class);

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {
    try {
      new ProcessBuilder()
          .command(getMavenExecutable(directory), "-B", "-DskipTests=true", "clean", "package")
          .directory(directory.toFile())
          .inheritIO()
          .start().waitFor();

      metadata.put(BuildStepMetadataConstants.BUILD_ARTIFACT_PATH, "target/");

    } catch (IOException | InterruptedException e) {
      throw new BuildStepException(e);
    }
  }

  // TODO refactor - this code is repeated in gradle build step
  private String getMavenExecutable(Path directory) {
    Path wrapperPath = directory.resolve("mvnw");
    if (Files.isExecutable(wrapperPath)) {
      logger.info("Maven wrapper discovered at {}. Using wrapper instead of system mvn.",
          wrapperPath.toString());
      return wrapperPath.toString();
    }

    String m2Home = System.getenv("M2_HOME");
    if (Strings.isNullOrEmpty(m2Home)) {
      throw new IllegalStateException("$M2_HOME must be set.");
    }
    Path systemMvn = Paths.get(m2Home).resolve("bin").resolve("mvn");
    if (Files.isExecutable(systemMvn)) {
      return systemMvn.toString();
    }

    throw new IllegalStateException(
        String.format("The file at %s is not a valid gradle executable", systemMvn.toString()));
  }

}
