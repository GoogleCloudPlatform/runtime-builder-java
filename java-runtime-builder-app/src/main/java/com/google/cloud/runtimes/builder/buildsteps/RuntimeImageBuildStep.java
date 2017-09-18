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
import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.APP_ENGINE_EXPLODED_WAR;
import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.EXPLODED_WAR;
import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.JAR;
import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.WAR;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.Artifact;
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
        : "$APP_DESTINATION";

    buildContext.getDockerfile().appendLine(copyStep + " " + relativeArtifactPath + " "
        + artifactDestination);
  }

  private String getBaseRuntimeImage(BuildContext buildContext, Artifact artifact)
      throws BuildStepException {
    RuntimeConfig runtimeConfig = buildContext.getRuntimeConfig();

    // Check if the user has explicitly selected the compat runtime
    if (buildContext.isForceCompatRuntime()) {
      if (artifact.getType() != APP_ENGINE_EXPLODED_WAR) {
        throw new BuildStepException(String.format("The App Engine java-compat runtime requires an "
            + "App Engine Exploded WAR artifact, but a %s artifact was provided (%s). In order to "
            + "proceed, modify your application to build an App Engine exploded WAR artifact. "
            + "See https://cloud.google.com/appengine/docs/flexible/java/upgrading for more "
            + "detail.", artifact.getType().toString(), artifact.toString()));
      }
      logger.info("Using base image '{}' for App Engine exploded WAR artifact", compatImageName);
      return compatImageName;
    }

    // Select runtime based on artifact type

    if (artifact.getType() == APP_ENGINE_EXPLODED_WAR || artifact.getType() == EXPLODED_WAR) {
      // Use the compat runtime for exploded war artifacts.
      logger.info("Using base image '{}' for exploded WAR artifact", compatImageName);
      return compatImageName;

    } else if (artifact.getType() == WAR) {
      String baseImage
          = jdkServerLookup.lookupServerImage(runtimeConfig.getJdk(), runtimeConfig.getServer());
      logger.info("Using base image '{}' for WAR artifact", baseImage);
      return baseImage;

    } else if (artifact.getType() == JAR) {
      // If the user expects a server to be involved, fail loudly.
      if (runtimeConfig.getServer() != null) {
        throw new BuildStepException("runtime_config.server configuration is not compatible with "
            + "JAR artifacts. To use a web server runtime, use a WAR artifact instead.");
      }
      String baseImage = jdkServerLookup.lookupJdkImage(runtimeConfig.getJdk());
      logger.info("Using base image '{}' for JAR artifact", baseImage);
      return baseImage;

    } else {
      throw new BuildStepException("Unable to select a base image for the artifact of type "
          + artifact.getType() + " at path " + artifact.getPath());
    }
  }

  /**
   * Returns the artifact to add to the runtime docker image.
   */
  protected abstract Artifact getArtifact(BuildContext buildContext) throws BuildStepException;

}
