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

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    Path artifact = getArtifact(buildContext);
    logger.debug("Found Java artifact {}", artifact);

    String baseImage;
    try {
      baseImage = getBaseRuntimeImage(buildContext, artifact);
    } catch (IOException e) {
      throw new BuildStepException("An error was encountered while searching for an artifact. "
          + "Please try again later.", e);
    }

    buildContext.getDockerfile().appendLine("FROM " + baseImage);
    String copyStep = "COPY ";
    if (buildContext.isSourceBuild()) {
      copyStep += "--from=" + DOCKERFILE_BUILD_STAGE + " ";
    }

    String relativeArtifactPath = "./" + buildContext.getWorkspaceDir().relativize(artifact)
        .toString();
    buildContext.getDockerfile().appendLine(copyStep + relativeArtifactPath + " $APP_DESTINATION");
  }

  private String getBaseRuntimeImage(BuildContext buildContext, Path artifact)
      throws BuildStepException, IOException {
    RuntimeConfig runtimeConfig = buildContext.getRuntimeConfig();

    // Runtime type is selected based on the file extension of the artifact. Then, the runtime image
    // is looked up using the provided runtime config fields.
    String extension = com.google.common.io.Files.getFileExtension(artifact.toString());
    if (extension.equalsIgnoreCase("war")) {
      String baseImage
          = jdkServerLookup.lookupServerImage(runtimeConfig.getJdk(), runtimeConfig.getServer());
      logger.info("Using base image '{}' for WAR artifact", baseImage);
      return baseImage;

    } else if (extension.equalsIgnoreCase("jar")) {
      // If the user expects a server to be involved, fail loudly.
      if (runtimeConfig.getServer() != null) {
        throw new BuildStepException("runtime_config.server configuration is not compatible with "
            + ".jar artifacts. To use a web server runtime, use a .war artifact instead.");
      }
      String baseImage = jdkServerLookup.lookupJdkImage(runtimeConfig.getJdk());
      logger.info("Using base image '{}' for JAR artifact", baseImage);
      return baseImage;

    } else if (Files.isSameFile(artifact, buildContext.getWorkspaceDir())
        && artifact.resolve("WEB-INF").resolve("appengine-web.xml").toFile().exists()) {
      // If the workspace directory itself is an exploded war, use the compat runtime.
      logger.info("Using base image '{}' for App Engine exploded WAR artifact", compatImageName);
      return compatImageName;

    } else {
      throw new BuildStepException("Unrecognized artifact: '" + artifact + "'. A .jar or .war "
          + "artifact was expected.");
    }
  }

  /**
   * Returns the artifact to add to the runtime docker image.
   */
  protected abstract Path getArtifact(BuildContext buildContext) throws BuildStepException;

}
