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

import static com.google.cloud.runtimes.builder.Constants.DOCKERFILE_BUILD_STAGE;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.injection.MavenDockerImage;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Build step that invokes maven.
 */
public class MavenBuildStep implements BuildStep {

  private static final String BUILD_CONTAINER_WORKDIR = "/build";
  private static final Logger logger = LoggerFactory.getLogger(MavenBuildStep.class);

  private final String mavenDockerImage;

  @Inject
  MavenBuildStep(@MavenDockerImage String mavenDockerImage) {
    this.mavenDockerImage = mavenDockerImage;
  }

  @Override
  public void run(BuildContext buildContext) throws BuildStepException {
    String dockerfileStep
        = "FROM " + mavenDockerImage + " as " + DOCKERFILE_BUILD_STAGE + "\n"
        + "WORKDIR " + BUILD_CONTAINER_WORKDIR + "\n"
        + "ADD . .\n"
        + "RUN " + getMavenExecutable(buildContext) + " -B -DskipTests clean install\n"
        + "\n";

    buildContext.getDockerfile().append(dockerfileStep);
    buildContext.setBuildArtifactLocation(Optional.of(
        Paths.get(BUILD_CONTAINER_WORKDIR, "target")));
  }

  private String getMavenExecutable(BuildContext buildContext) {
    Path wrapperPath = buildContext.getWorkspaceDir().resolve("mvnw");
    if (Files.exists(wrapperPath)) {
      Path relativePath = buildContext.getWorkspaceDir().relativize(wrapperPath);
      logger.info("Maven wrapper discovered at {}. Using wrapper instead of system mvn.",
          relativePath);

      return Paths.get(BUILD_CONTAINER_WORKDIR).resolve(relativePath).toString();
    }
    return "mvn";
  }
}
