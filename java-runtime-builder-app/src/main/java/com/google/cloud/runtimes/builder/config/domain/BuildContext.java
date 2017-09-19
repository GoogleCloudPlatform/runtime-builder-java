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

package com.google.cloud.runtimes.builder.config.domain;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import com.google.cloud.runtimes.builder.injection.DisableSourceBuild;
import com.google.cloud.runtimes.builder.util.StringLineAppender;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Encapsulates information about the build's state. Mediates interactions with the build directory
 * and stores build configuration.
 */
public class BuildContext {

  private static final String DOCKERFILE_NAME = "Dockerfile";
  private static final String DOCKERIGNORE_NAME = ".dockerignore";

  private final Logger logger = LoggerFactory.getLogger(BuildContext.class);

  private final AppYaml appYaml;
  private final Path workspaceDir;
  private final StringLineAppender dockerfile;
  private final StringLineAppender dockerignore;
  private final boolean disableSourceBuild;

  private Optional<Path> buildArtifactLocation;

  /**
   * Constructs a new {@link BuildContext}.
   *
   * @param appYaml configuration object provided by the user
   * @param workspaceDir the directory in which the build will take place
   * @param disableSourceBuild whether or not source builds should be disabled
   */
  @Inject
  @VisibleForTesting
  public BuildContext(@Assisted AppYaml appYaml, @Assisted Path workspaceDir,
      @DisableSourceBuild boolean disableSourceBuild) {
    Preconditions.checkArgument(Files.isDirectory(workspaceDir));

    this.appYaml = appYaml;
    this.workspaceDir = workspaceDir;
    this.dockerfile = new StringLineAppender();
    // dockerignore should always include itself and the dockerfile
    this.dockerignore = new StringLineAppender(DOCKERFILE_NAME, DOCKERIGNORE_NAME);
    this.disableSourceBuild = disableSourceBuild;

    buildArtifactLocation = Optional.empty();
  }

  public RuntimeConfig getRuntimeConfig() {
    return appYaml.getRuntimeConfig();
  }

  public Path getWorkspaceDir() {
    return workspaceDir;
  }

  public StringLineAppender getDockerfile() {
    return dockerfile;
  }

  public StringLineAppender getDockerignore() {
    return dockerignore;
  }

  public Optional<Path> getBuildArtifactLocation() {
    return buildArtifactLocation;
  }

  public void setBuildArtifactLocation(Optional<Path> buildArtifactLocation) {
    this.buildArtifactLocation = buildArtifactLocation;
  }

  /**
   * Returns true if this is a build from source that requires compilation or some other build step
   * to generate a build artifact, false if otherwise.
   */
  public boolean isSourceBuild() {
    try {
      return !disableSourceBuild && (!Strings.isNullOrEmpty(
          appYaml.getRuntimeConfig().getBuildScript()) || getBuildTool().isPresent());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns whether or not a compat runtime was requested in the user's configuration.
   *
   * @return true if the user has explicitly requested a compat runtime
   */
  public boolean isCompatEnabled() {
    return appYaml.getBetaSettings().isEnableAppEngineApis();
  }

  /**
   * Writes the contents of Dockerfile and .dockerignore buffers to files. If a .dockerignore file
   * already exists, the .dockerignore buffer will be appended to it.
   *
   * @throws IOException if a transient error occurs while writing the files
   */
  public void writeDockerResources() throws IOException {
    writeDockerFile();
    writeDockerIgnore();
  }

  private void writeDockerFile() throws IOException {
    Path dockerFilePath = workspaceDir.resolve(DOCKERFILE_NAME);

    // fail loudly if a Dockerfile already exists
    if (Files.exists(dockerFilePath)) {
      throw new IllegalStateException("Custom Dockerfiles are not supported. If you wish to use a "
          + "custom Dockerfile, consider using runtime: custom. Otherwise, remove the Dockerfile "
          + "from the root of your sources to continue.");
    }

    // write Dockerfile
    logger.info("Generating Dockerfile at {}", dockerFilePath);
    try (BufferedWriter writer = Files.newBufferedWriter(dockerFilePath)) {
      writer.write(dockerfile.toString());
    }
  }

  private void writeDockerIgnore() throws IOException {
    // If there's nothing to write, return
    if (dockerignore.getLines().size() < 1) {
      return;
    }

    Path dockerIgnorePath = workspaceDir.resolve(DOCKERIGNORE_NAME);
    Set<String> existingDockerignoreLines = new HashSet<>();

    // Read the contents of an existing .dockerignore file to avoid duplicating lines
    if (Files.exists(dockerIgnorePath)) {
      try (BufferedReader reader = Files.newBufferedReader(dockerIgnorePath)) {
        reader.lines().forEach(existingDockerignoreLines::add);
      }
    }

    // Filter out lines that are already present
    dockerignore.setLines(dockerignore.getLines().stream()
        .filter(line -> !existingDockerignoreLines.contains(line))
        .collect(Collectors.toList()));

    // Make sure there are lines remaining before proceeding
    if (dockerignore.getLines().size() < 1) {
      return;
    }

    // Write the dockerignore file
    logger.info("Generating .dockerignore file at {}", dockerIgnorePath);
    try (BufferedWriter writer
        = Files.newBufferedWriter(dockerIgnorePath, CREATE, WRITE, APPEND)) {
      // prepend with a newline character
      dockerignore.prependLine();
      writer.write(dockerignore.toString());
    }
  }

  /**
   * Finds the build tool that should be used to build the source directory, if any.
   */
  public Optional<BuildTool> getBuildTool() throws IOException {
    return Files.list(workspaceDir)
        .filter((path) -> Files.isRegularFile(path))
        .filter(BuildTool::isABuildFile)
        // sort based on natural ordering of BuildTool for each path
        .sorted(Comparator.comparing(BuildTool::getForBuildFile))
        .findFirst()
        .map(BuildTool::getForBuildFile);
  }
}
