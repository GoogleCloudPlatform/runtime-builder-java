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

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.exception.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.exception.TooManyArtifactsException;
import com.google.cloud.runtimes.builder.injection.CompatDockerImage;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class PrebuiltRuntimeImageBuildStep extends RuntimeImageBuildStep {

  private final Logger logger = LoggerFactory.getLogger(RuntimeImageBuildStep.class);

  @Inject
  PrebuiltRuntimeImageBuildStep(JdkServerLookup jdkServerLookup,
      @CompatDockerImage String compatImageName) {
    super(jdkServerLookup, compatImageName);
  }

  @Override
  protected Path getArtifact(BuildContext buildContext) throws BuildStepException {
    String providedArtifactPath = buildContext.getRuntimeConfig().getArtifact();
    if (providedArtifactPath != null) {
      // if the artifact path is set in runtime configuration, use that value
      return buildContext.getWorkspaceDir().resolve(providedArtifactPath);
    }

    List<Path> artifacts;
    try {
      artifacts = findArtifacts(buildContext.getWorkspaceDir());
    } catch (IOException e) {
      throw new BuildStepException(e);
    }

    if (artifacts.size() < 1) {
      throw new ArtifactNotFoundException();
    } else if (artifacts.size() > 1) {
      throw new TooManyArtifactsException(artifacts);
    } else {
      Path artifact = artifacts.get(0);
      logger.debug("Found Java artifact {}", artifact);
      return artifact;
    }
  }

  /*
   * Searches non-recursively for deployable artifacts in the given directory. Potential artifacts
   * include:
   * - files or directories ending in .jar or .war
   * - the current directory itself, if it is an exploded war
   */
  private List<Path> findArtifacts(Path searchDir) throws IOException {
    List<Path> artifacts = Files.list(searchDir)
        .filter(path -> {
          String extension = com.google.common.io.Files.getFileExtension(path.toString());
          return extension.equalsIgnoreCase("war") || extension.equalsIgnoreCase("jar");
        })
        .collect(Collectors.toList());

    // If the search directory contains a compat deployment descriptor, it is a valid artifact
    if (Files.exists(searchDir.resolve("WEB-INF").resolve("appengine-web.xml"))) {
      artifacts.add(searchDir);
    }

    return artifacts;
  }
}
