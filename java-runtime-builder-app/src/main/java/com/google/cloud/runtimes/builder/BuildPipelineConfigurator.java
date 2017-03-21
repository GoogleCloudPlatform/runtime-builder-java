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
import com.google.cloud.runtimes.builder.config.YamlParser;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.BuildTool;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import com.fasterxml.jackson.databind.JsonMappingException;
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

  private static final List<String> APP_YAML_LOCATIONS
      = ImmutableList.of("app.yaml", "src/main/appengine/app.yaml");

  private final Logger logger = LoggerFactory.getLogger(BuildPipelineConfigurator.class);

  private final YamlParser<AppYaml> appYamlParser;
  private final BuildStepFactory buildStepFactory;

  @Inject
  BuildPipelineConfigurator(YamlParser<AppYaml> appYamlParser, BuildStepFactory buildStepFactory) {
    this.appYamlParser = appYamlParser;
    this.buildStepFactory = buildStepFactory;
  }

  /**
   * Examines a directory and returns a list of build steps that should be executed on it.
   *
   * @throws AppYamlNotFoundException if an app.yaml config file is not found
   * @throws IOException if a transient file system error is encountered
   */
  public List<BuildStep> configurePipeline(Path workspaceDir)
      throws AppYamlNotFoundException, IOException {
    // locate and deserialize configuration files
    Path pathToAppYaml = findAppYaml(workspaceDir);
    AppYaml appYaml;
    try {
      appYaml = appYamlParser.parse(pathToAppYaml);
    } catch (JsonMappingException e) {
      logger.error("There was an error parsing app.yaml file located at {}. Please make sure it is "
          + "a valid yaml file.", pathToAppYaml, e);
      throw e;
    }

    RuntimeConfig runtimeConfig = appYaml.getRuntimeConfig() != null
        ? appYaml.getRuntimeConfig()
        : new RuntimeConfig();

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
   * Searches for app.yaml in a few expected paths within the workspace
   */
  private Path findAppYaml(Path workspaceDir) throws AppYamlNotFoundException {
    return APP_YAML_LOCATIONS.stream()
        .map(pathName -> workspaceDir.resolve(pathName))
        .filter(path -> Files.exists(path) && Files.isRegularFile(path))
        .findFirst()
        .orElseThrow(() -> new AppYamlNotFoundException("An app.yaml configuration file is "
            + "required, but was not found in the included sources."));
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
