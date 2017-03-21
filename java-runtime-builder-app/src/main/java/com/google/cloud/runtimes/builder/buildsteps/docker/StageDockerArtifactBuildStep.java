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

package com.google.cloud.runtimes.builder.buildsteps.docker;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepMetadataConstants;
import com.google.cloud.runtimes.builder.exception.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.exception.TooManyArtifactsException;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StageDockerArtifactBuildStep extends BuildStep {

  private final Logger logger = LoggerFactory.getLogger(StageDockerArtifactBuildStep.class);
  private final DockerfileGenerator dockerfileGenerator;
  private String artifactPathOverride;

  @Inject
  StageDockerArtifactBuildStep(DockerfileGenerator dockerfileGenerator) {
    this.dockerfileGenerator = dockerfileGenerator;
  }

  public void setArtifactPathOverride(String artifactPathOverride) {
    this.artifactPathOverride = artifactPathOverride;
  }

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {
    try {
      // TODO wrap this in a try block and log a more friendly message if not found
      Path artifact = getArtifact(directory, metadata);
      logger.info("Found artifact {}", artifact);

      // make staging dir
      // TODO delete dir if exists
      Path stagingDir = Files.createDirectory(directory.resolve(".docker_staging"));
      metadata.put(BuildStepMetadataConstants.DOCKER_STAGING_PATH, stagingDir.toString());

      logger.info("Preparing docker files in {}", stagingDir);

      // copy the artifact into the staging dir
      Files.copy(artifact, stagingDir.resolve(artifact.getFileName()));

      // Generate dockerfile
      String dockerfile = dockerfileGenerator.generateDockerfile(artifact.getFileName());
      Path dockerFileDest = stagingDir.resolve("Dockerfile");

      try (BufferedWriter writer
          = Files.newBufferedWriter(dockerFileDest, StandardCharsets.US_ASCII)) {
        writer.write(dockerfile);
      }
    } catch (IOException | ArtifactNotFoundException | TooManyArtifactsException e) {
      throw new BuildStepException(e);
    }
  }

  /*
   * Returns the artifact to use for staging.
   */
  private Path getArtifact(Path directory, Map<String, String> metadata)
      throws ArtifactNotFoundException, IOException, TooManyArtifactsException {
    // if the artifact path has been overridden, use that value
    if (artifactPathOverride != null) {
      return directory.resolve(artifactPathOverride);
    } else if (metadata.containsKey(BuildStepMetadataConstants.BUILD_ARTIFACT_PATH)) {
      // if the artifact path is found in the metadata
      String buildArtifactPath = metadata.get(BuildStepMetadataConstants.BUILD_ARTIFACT_PATH);
      Path buildOutputDir = directory.resolve(buildArtifactPath);
      return searchForArtifactInDir(buildOutputDir);
    } else {
      // otherwise, search for an artifact in the workspace root
      return searchForArtifactInDir(directory);
    }
  }

  /*
   * Searches for files that look like deployable artifacts in the given directory
   */
  private Path searchForArtifactInDir(Path directory) throws ArtifactNotFoundException,
      TooManyArtifactsException, IOException {
    logger.info("Searching for a deployable artifact in {}", directory.toString());
    if (!Files.isDirectory(directory)) {
      throw new IllegalArgumentException(String.format("%s is not a valid directory.", directory));
    }

    List<Path> validArtifacts = new ArrayList<>();
    Files.list(directory)
        // filter out files that don't end in .war or .jar
        .filter((path) -> {
          String extension = com.google.common.io.Files.getFileExtension(path.toString());
          return extension.equals("war") || extension.equals("jar");
        })
        .forEach(validArtifacts::add);

    if (validArtifacts.size() < 1) {
      throw new ArtifactNotFoundException();
    } else if (validArtifacts.size() > 1) {
      throw new TooManyArtifactsException(validArtifacts);
    } else {
      return validArtifacts.get(0);
    }
  }


}
