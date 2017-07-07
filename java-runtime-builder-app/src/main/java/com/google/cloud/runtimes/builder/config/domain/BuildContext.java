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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BuildContext {

  private static final String DOCKERFILE_NAME = "Dockerfile";
  private static final String DOCKERIGNORE_NAME = ".dockerignore";

  private final RuntimeConfig runtimeConfig;
  private final Path workspaceDir;
  private final StringBuilder dockerfile;
  private final StringBuilder dockerignore;

  private Optional<Path> buildArtifactLocation;

  /**
   * Constructs a new {@link BuildContext}.
   *
   * @param runtimeConfig runtime configuration object provided by the user
   * @param workspaceDir the directory in which the build will take place
   */
  public BuildContext(RuntimeConfig runtimeConfig, Path workspaceDir) {
    Preconditions.checkArgument(Files.isDirectory(workspaceDir));

    this.runtimeConfig = runtimeConfig;
    this.workspaceDir = workspaceDir;
    this.dockerfile = new StringBuilder();
    this.dockerignore = new StringBuilder();

    buildArtifactLocation = Optional.empty();
  }

  public RuntimeConfig getRuntimeConfig() {
    return runtimeConfig;
  }

  public Path getWorkspaceDir() {
    return workspaceDir;
  }

  public StringBuilder getDockerfile() {
    return dockerfile;
  }

  public StringBuilder getDockerignore() {
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
      return !Strings.isNullOrEmpty(runtimeConfig.getBuildScript()) || getBuildTool().isPresent();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Writes the contents of Dockerfile and .dockerignore buffers to files. If a .dockerignore file
   * already exists, the .dockerignore buffer will be appended to it.
   *
   * @throws IOException if a transient error occurs while writing the files
   */
  public void writeDockerFiles() throws IOException {
    // write Dockerfile
    try (BufferedWriter writer = Files.newBufferedWriter(workspaceDir.resolve(DOCKERFILE_NAME))) {
      writer.write(dockerfile.toString());
    }

    // write .dockerignore file, appending to an existing file if it exists
    try (BufferedWriter writer = Files.newBufferedWriter(workspaceDir.resolve(DOCKERIGNORE_NAME),
        CREATE, WRITE, APPEND)) {
      // write a newline in case an existing .dockerignore doesn't end with a newline character
      writer.newLine();
      writer.write(dockerignore.toString());
    }
  }

  /**
   * Searches for files that look like deployable artifacts in the given directory.
   */
  public List<Path> findArtifacts() throws IOException {
    return Files.list(workspaceDir)
        // filter out files that don't end in .war or .jar
        .filter((path) -> {
          String extension = com.google.common.io.Files.getFileExtension(path.toString());
          return extension.equals("war") || extension.equals("jar");
        })
        .collect(Collectors.toList());
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
