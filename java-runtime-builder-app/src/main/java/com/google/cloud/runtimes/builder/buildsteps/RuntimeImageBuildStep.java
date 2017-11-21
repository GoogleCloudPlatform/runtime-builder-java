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
import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.COMPAT_EXPLODED_WAR;
import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.EXPLODED_WAR;
import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.JAR;
import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.WAR;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.Artifact;
import com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RuntimeImageBuildStep implements BuildStep {

  private final Logger logger = LoggerFactory.getLogger(RuntimeImageBuildStep.class);

  private final JdkServerLookup jdkServerLookup;
  private final String compatImageName;

  protected RuntimeImageBuildStep(JdkServerLookup jdkServerLookup, String compatImageName) {
    this.jdkServerLookup = jdkServerLookup;
    this.compatImageName = compatImageName;
  }

  @Override
  public void run(BuildContext buildContext) throws BuildStepException {
    Artifact artifact = getArtifact(buildContext);
    logger.info("Identified Java artifact for deployment {}", artifact);

    String baseRuntimeImage = getBaseRuntimeImage(buildContext, artifact);
    buildContext.getDockerfile().appendLine("FROM " + baseRuntimeImage);
    String copyStep = "COPY";
    if (buildContext.isSourceBuild()) {
      copyStep += " --from=" + DOCKERFILE_BUILD_STAGE;
    }

    String relativeArtifactPath = "./" + buildContext.getWorkspaceDir()
        .relativize(artifact.getPath()).toString();

    // compat runtime requires a special app destination
    String artifactDestination = baseRuntimeImage.equals(compatImageName)
        ? "/app/"
        : (artifact.getType() == EXPLODED_WAR ? "$APP_DESTINATION_EXPLODED_WAR"
            : "$APP_DESTINATION");

    buildContext.getDockerfile().appendLine(copyStep + " " + relativeArtifactPath + " "
        + artifactDestination);
  }

  private String getBaseRuntimeImage(BuildContext buildContext, Artifact artifact)
      throws BuildStepException {
    RuntimeConfig runtimeConfig = buildContext.getRuntimeConfig();
    ArtifactType artifactType = artifact.getType();
    String baseImage;

    if (artifactType == COMPAT_EXPLODED_WAR) {
      baseImage = compatImageName;
    } else if (buildContext.isCompatEnabled()) {
      if (artifactType != EXPLODED_WAR) {
        throw new BuildStepException(String.format("App Engine APIs have been enabled. In order to "
            + "use App Engine APIs, an exploded WAR artifact is required, but a %s artifact was "
            + "found. See https://cloud.google.com/appengine/docs/flexible/java/upgrading for more "
            + "detail.", artifact.getType()));
      }
      baseImage = compatImageName;
    } else if (artifactType == EXPLODED_WAR || artifactType == WAR) {
      baseImage = jdkServerLookup
          .lookupServerImage(runtimeConfig.getJdk(), runtimeConfig.getServer());
    } else if (artifactType == JAR) {
      // If the user expects a server to be involved, fail loudly.
      if (runtimeConfig.getServer() != null) {
        throw new BuildStepException("runtime_config.server configuration is not compatible with "
            + "JAR artifacts. To use a web server runtime, use a WAR artifact instead.");
      }
      baseImage = jdkServerLookup.lookupJdkImage(runtimeConfig.getJdk());
    } else {
      throw new BuildStepException("Unable to select a base image for the artifact of type "
          + artifactType + " at path " + artifact.getPath());
    }
    logger.info("Using base image '{}' for {} artifact", baseImage, artifactType);
    return baseImage;
  }

  /**
   * Returns the artifact to add to the runtime docker image.
   */
  protected abstract Artifact getArtifact(BuildContext buildContext) throws BuildStepException;

}
