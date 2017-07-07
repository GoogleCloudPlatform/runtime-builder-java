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

package com.google.cloud.runtimes.builder.buildsteps;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Build step that invokes maven.
 */
public class MavenBuildStep implements BuildStep {

  private static final String DEFAULT_ARTIFACT_PATH = "target";
  private static final String DOCKERFILE_STEP = "FROM gcr.io/cloud-builders/java/mvn\n"
      + "RUN %s -B -DskipTests=true clean package\n"
      + "\n";

  private static final Logger logger = LoggerFactory.getLogger(MavenBuildStep.class);

  @Override
  public void run(BuildContext buildContext) throws BuildStepException {
    String mvnExecutable = getMavenExecutable(buildContext.getWorkspaceDir());
    buildContext.getDockerfile().append(String.format(DOCKERFILE_STEP, mvnExecutable));
    buildContext.setBuildArtifactLocation(
        Optional.of(buildContext.getWorkspaceDir().resolve(DEFAULT_ARTIFACT_PATH)));
  }

  private String getMavenExecutable(Path directory) {
    Path wrapperPath = directory.resolve("mvnw");
    if (Files.exists(wrapperPath)) {
      logger.info("Maven wrapper discovered at {}. Using wrapper instead of system mvn.",
          wrapperPath.toString());
      return wrapperPath.toString();
    }
    return "mvn";
  }
}
