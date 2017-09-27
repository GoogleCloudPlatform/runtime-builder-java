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

import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.EXPLODED_WAR;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.Artifact;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.exception.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.exception.TooManyArtifactsException;
import com.google.cloud.runtimes.builder.injection.CompatDockerImage;
import com.google.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class PrebuiltRuntimeImageBuildStep extends RuntimeImageBuildStep {

  @Inject
  PrebuiltRuntimeImageBuildStep(JdkServerLookup jdkServerLookup,
      @CompatDockerImage String compatImageName) {
    super(jdkServerLookup, compatImageName);
  }

  @Override
  protected Artifact getArtifact(BuildContext buildContext) throws BuildStepException {
    String providedArtifactPath = buildContext.getRuntimeConfig().getArtifact();
    if (providedArtifactPath != null) {
      // if the artifact path is set in runtime configuration, use that value
      return Artifact.fromPath(buildContext.getWorkspaceDir().resolve(providedArtifactPath));
    }

    List<Path> artifacts;
    try {
      // Check if the workspace itself is an exploded war artifact
      if (Artifact.isAnArtifact(buildContext.getWorkspaceDir())) {
        Artifact rootArtifact = Artifact.fromPath(buildContext.getWorkspaceDir());
        if (rootArtifact.getType() == EXPLODED_WAR) {
          return rootArtifact;
        }
      }

      // Potential artifacts include all files (not including directories) at the workspace root.
      artifacts = Files.list(buildContext.getWorkspaceDir())
          // filter out directories
          .filter((path) -> !Files.isDirectory(path))
          // filter out non-artifacts
          .filter(Artifact::isAnArtifact)
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new BuildStepException(e);
    }

    if (artifacts.size() < 1) {
      throw new ArtifactNotFoundException();
    } else if (artifacts.size() > 1) {
      throw new TooManyArtifactsException(artifacts);
    } else {
      return Artifact.fromPath(artifacts.get(0));
    }
  }

}
