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

package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepFactory;
import com.google.cloud.runtimes.builder.buildsteps.docker.StageDockerArtifactBuildStep;
import com.google.cloud.runtimes.builder.config.ConfigParser;
import com.google.cloud.runtimes.builder.config.domain.BuildTool;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Fingerprints a directory and determines what set of build steps should be performed on it.
 */
public class BuildPipelineConfigurator {

  private static final String CONFIG_ENV_VARIABLE = "GCP_RUNTIME_BUILDER_CONFIG";

  private final Logger logger = LoggerFactory.getLogger(BuildPipelineConfigurator.class);
  private final ConfigParser configParser;
  private final BuildStepFactory buildStepFactory;

  @Inject
  BuildPipelineConfigurator(ConfigParser configParser, BuildStepFactory buildStepFactory) {
    this.configParser = configParser;
    this.buildStepFactory = buildStepFactory;
  }

  /**
   * Examines a directory and returns a list of build steps that should be executed on it.
   *
   * @throws IOException if a transient file system error is encountered
   */
  public List<BuildStep> configurePipeline(Path workspaceDir) throws IOException {
    // attempt to deserialize configuration
    RuntimeConfig runtimeConfig = configParser.parseFromEnvVar(CONFIG_ENV_VARIABLE);

    // assemble the list of build steps
    List<BuildStep> steps = new ArrayList<>();

    String buildScript = runtimeConfig.getBuildScript();
    if (!Strings.isNullOrEmpty(buildScript)) {
      // the user has specified a custom command to build the project
      steps.add(buildStepFactory.createScriptExecutionBuildStep(buildScript));
    } else {
      // search for build files in the workspace
      Optional<Path> buildFile = findBuildFile(workspaceDir);
      if (buildFile.isPresent()) {
        // select the correct build step for the buildTool
        BuildTool buildTool = BuildTool.getForBuildFile(buildFile.get());
        steps.add(getBuildStepForTool(buildTool));
      }
    }

    StageDockerArtifactBuildStep stageDockerBuildStep
        = buildStepFactory.createStageDockerArtifactBuildStep();
    stageDockerBuildStep.setArtifactPathOverride(runtimeConfig.getArtifact());
    steps.add(stageDockerBuildStep);
    return steps;
  }

  /*
   * Selects a build step for a build tool.
   */
  private BuildStep getBuildStepForTool(BuildTool buildTool) {
    switch (buildTool) {
      case MAVEN:
        return buildStepFactory.createMavenBuildStep();
      case GRADLE:
        return buildStepFactory.createGradleBuildStep();
      default:
        throw new IllegalArgumentException(
            String.format("No build step available for build tool %s", buildTool));
    }
  }

  /*
   * Attempt to find a file in the workspace that looks like a build file.
   */
  private Optional<Path> findBuildFile(Path workspaceDir) throws IOException {
    return Files.list(workspaceDir)
        .filter((path) -> Files.isRegularFile(path))
        .filter(BuildTool::isABuildFile)
        // sort based on natural ordering of BuildTool for each path
        .sorted(Comparator.comparing(BuildTool::getForBuildFile))
        .findFirst();
  }
}
