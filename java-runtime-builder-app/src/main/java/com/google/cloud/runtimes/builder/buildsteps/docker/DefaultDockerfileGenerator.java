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

import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

import com.google.common.io.Files;
import com.google.inject.Inject;

import java.nio.file.Path;

/**
 * Default implementation of a {@link DockerfileGenerator}.
 */
public class DefaultDockerfileGenerator implements DockerfileGenerator {

  private static final String DOCKERFILE_FROM = "FROM %s\n";
  private static final String DOCKERFILE_ADD_ARTIFACT = "ADD %s %s\n";
  private static final String DOCKERFILE_JETTY_QUICKSTART = "RUN /scripts/jetty/quickstart.sh\n";

  private static final String APP_DESTINATION = "$APP_DEST";

  private final JdkServerLookup runtimeLookupTool;

  /**
   * Constructs a new {@link DefaultDockerfileGenerator}.
   */
  @Inject
  DefaultDockerfileGenerator(JdkServerLookup runtimeLookupTool) {
    this.runtimeLookupTool = runtimeLookupTool;
  }

  @Override
  public String generateDockerfile(Path artifactToDeploy, RuntimeConfig runtimeConfig) {
    StringBuilder dockerfile = new StringBuilder();
    String fileType = Files.getFileExtension(artifactToDeploy.toString());
    String baseImage;

    if (fileType.equalsIgnoreCase("jar")) {
      baseImage = runtimeLookupTool.lookupJdkImage(runtimeConfig.getJdk());
    } else if (fileType.equalsIgnoreCase("war")) {
      baseImage = runtimeLookupTool.lookupServerImage(runtimeConfig.getJdk(),
          runtimeConfig.getServer());
    } else {
      throw new IllegalArgumentException(
          String.format("Unable to determine the runtime for artifact %s. Expected a .jar or .war "
                  + "file.",
              artifactToDeploy.getFileName()));
    }
    dockerfile.append(String.format(DOCKERFILE_FROM, baseImage));
    dockerfile.append(String.format(DOCKERFILE_ADD_ARTIFACT, artifactToDeploy.toString(),
        APP_DESTINATION));

    if (baseImage.contains("jetty")) {
      addJettyConfiguration(runtimeConfig, dockerfile);
    }

    return dockerfile.toString();
  }

  private void addJettyConfiguration(RuntimeConfig runtimeConfig, StringBuilder dockerfile) {
    // apply jetty-specific configuration, if present
    if (runtimeConfig.getJettyQuickstart()) {
      dockerfile.append(DOCKERFILE_JETTY_QUICKSTART);
    }
  }

}
