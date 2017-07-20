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

import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.injection.CompatDockerImage;
import com.google.inject.Inject;

import java.nio.file.Path;

public class SourceBuildRuntimeImageBuildStep extends RuntimeImageBuildStep {

  @Inject
  SourceBuildRuntimeImageBuildStep(JdkServerLookup jdkServerLookup,
      @CompatDockerImage String compatImage) {
    super(jdkServerLookup, compatImage);
  }

  @Override
  protected Path getArtifact(BuildContext buildContext) {
    String providedArtifactPath = buildContext.getRuntimeConfig().getArtifact();

    // Require that the artifact name is explicitly provided by the user.
    if (providedArtifactPath == null) {
      throw new IllegalStateException("Unable to determine the artifact path. In order to build "
          + "from source, the path to the artifact must be specified in the runtime_config.artifact"
          + " field.");
    }
    return buildContext.getWorkspaceDir().resolve(providedArtifactPath);
  }

}
