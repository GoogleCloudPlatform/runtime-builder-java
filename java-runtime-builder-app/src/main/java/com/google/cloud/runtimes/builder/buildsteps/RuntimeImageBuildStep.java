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

public abstract class RuntimeImageBuildStep implements BuildStep {

  private final JdkServerLookup jdkServerLookup;

  protected RuntimeImageBuildStep(JdkServerLookup jdkServerLookup) {
    this.jdkServerLookup = jdkServerLookup;
  }

  @Override
  public void run(BuildContext buildContext) throws BuildStepException {
    buildContext.getDockerfile().appendLine("FROM " + getBaseRuntimeImage(buildContext));

    String copyStep = "COPY ";
    if (buildContext.isSourceBuild()) {
      copyStep += "--from=" + DOCKERFILE_BUILD_STAGE + " ";
    }
    buildContext.getDockerfile().appendLine(copyStep + getArtifact(buildContext)
        + " $APP_DESTINATION");
  }

  private String getBaseRuntimeImage(BuildContext buildContext) throws BuildStepException {
    String artifact = getArtifact(buildContext);
    RuntimeConfig runtimeConfig = buildContext.getRuntimeConfig();

    // Runtime type (web server vs plain JDK) runtime is selected based on the file extension of the
    // artifact. Then, the runtime image is looked up using the provided runtime config fields.
    if (artifact.endsWith("war") || artifact.endsWith("WAR")) {
      return jdkServerLookup.lookupServerImage(runtimeConfig.getJdk(), runtimeConfig.getServer());
    } else if (artifact.endsWith("jar") || artifact.endsWith("JAR")) {
      // If the user expects a server to be involved, fail loudly.
      if (runtimeConfig.getServer() != null) {
        throw new BuildStepException("runtime_config.server configuration is not compatible with "
            + ".jar artifacts. To use a web server runtime, use a .war artifact instead.");
      }
      return jdkServerLookup.lookupJdkImage(runtimeConfig.getJdk());
    } else {
      throw new BuildStepException("Unrecognized artifact: '" + artifact + "'. A .jar or .war "
          + "artifact was expected.");
    }
  }

  /**
   * Returns the artifact to add to the runtime docker image.
   */
  protected abstract String getArtifact(BuildContext buildContext) throws BuildStepException;

}
