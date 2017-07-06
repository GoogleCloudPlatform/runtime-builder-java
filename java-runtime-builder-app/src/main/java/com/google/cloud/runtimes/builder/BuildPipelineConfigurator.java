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
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepFactory;
import com.google.cloud.runtimes.builder.config.AppYamlFinder;
import com.google.cloud.runtimes.builder.config.YamlParser;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.BuildTool;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

/**
 * Fingerprints a directory and determines what set of build steps should be performed on it.
 */
public class BuildPipelineConfigurator {

  private final Logger logger = LoggerFactory.getLogger(BuildPipelineConfigurator.class);

  private final YamlParser<AppYaml> appYamlParser;
  private final AppYamlFinder appYamlFinder;
  private final BuildStepFactory buildStepFactory;

  @Inject
  BuildPipelineConfigurator(YamlParser<AppYaml> appYamlParser, AppYamlFinder appYamlFinder,
      BuildStepFactory buildStepFactory) {
    this.appYamlParser = appYamlParser;
    this.appYamlFinder = appYamlFinder;
    this.buildStepFactory = buildStepFactory;
  }

  public void generateDockerResources(Path workspaceDir) throws AppYamlNotFoundException,
      IOException, BuildStepException {

    BuildContext buildContext = configureBuildContext(workspaceDir);

    String buildScript = buildContext.getRuntimeConfig().getBuildScript();
    if (!Strings.isNullOrEmpty(buildScript)) {
      // the user has specified a custom command to build the project
      buildStepFactory.createScriptExecutionBuildStep(buildScript).run(buildContext);
    } else {
      // search for build files in the workspace
      Optional<Path> buildFile = findBuildFile(workspaceDir);
      if (buildFile.isPresent()) {
        // select the correct build step for the buildTool
        BuildTool buildTool = BuildTool.getForBuildFile(buildFile.get());
        getBuildStepForTool(buildTool).run(buildContext);
      }
    }

    buildStepFactory.createRuntimeImageBuildStep().run(buildContext);

    // TODO write Dockerfile and .dockerignore to files
    // TODO also, make sure to honor an existing .dockerignore by appending to it rather than overwriting
  }

  private BuildContext configureBuildContext(Path workspaceDir) throws AppYamlNotFoundException,
      IOException {

    // locate and deserialize configuration files
    Optional<Path> pathToAppYaml = appYamlFinder.findAppYamlFile(workspaceDir);

    AppYaml appYaml;
    if (pathToAppYaml.isPresent()) {
      appYaml = parseAppYaml(pathToAppYaml.get());
    } else {
      appYaml = new AppYaml();
    }

    RuntimeConfig runtimeConfig = appYaml.getRuntimeConfig() != null
        ? appYaml.getRuntimeConfig()
        : new RuntimeConfig();

    // custom Dockerfiles are not supported - fail loudly so there is no ambiguity about the image
    // being built
    if (findDockerfile(workspaceDir).isPresent()) {
      throw new IllegalStateException("Custom Dockerfiles are not supported. If you wish to use a "
          + "custom Dockerfile, consider using runtime: custom. Otherwise, remove the Dockerfile "
          + "from the root of your sources to continue.");
    }

    return new BuildContext(runtimeConfig, workspaceDir);
  }

  private AppYaml parseAppYaml(Path pathToAppYaml) throws IOException {
    try {
      return appYamlParser.parse(pathToAppYaml);
    } catch (JsonMappingException e) {
      logger.error("There was an error parsing the config file located at {}. Please make sure it "
          + "is a valid yaml file.", pathToAppYaml, e);
      throw e;
    }
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

  /*
   * Search for a Dockerfile at the root of the workspace
   */
  private Optional<Path> findDockerfile(Path workspaceDir) {
    Path dockerfilePath = workspaceDir.resolve("Dockerfile");
    if (Files.exists(dockerfilePath) && Files.isRegularFile(dockerfilePath)) {
      return Optional.of(dockerfilePath);
    } else {
      return Optional.empty();
    }
  }
}
