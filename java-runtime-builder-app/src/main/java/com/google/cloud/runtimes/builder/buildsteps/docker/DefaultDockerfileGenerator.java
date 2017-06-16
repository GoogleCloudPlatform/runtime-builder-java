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

import com.google.cloud.runtimes.builder.config.domain.JdkServerMap;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

import com.google.common.io.Files;
import com.google.inject.Inject;

import java.nio.file.Path;

/**
 * Default implementation of a {@link DockerfileGenerator}.
 */
public class DefaultDockerfileGenerator implements DockerfileGenerator {

  private static final String DOCKERFILE = "FROM %s\n"
      + "ADD %s %s\n";

  private final JdkServerMap jdkServerMap;

  /**
   * Constructs a new {@link DefaultDockerfileGenerator}.
   */
  @Inject
  DefaultDockerfileGenerator(JdkServerMap jdkServerMap) {
    this.jdkServerMap = jdkServerMap;
  }

  @Override
  public String generateDockerfile(Path artifactToDeploy, RuntimeConfig runtimeConfig) {
    StringBuilder dockerfile = new StringBuilder();
    String fileType = Files.getFileExtension(artifactToDeploy.toString());
    String baseImage;
    String appDest = "$APP_DEST"; // TODO make sure all runtimes support this

    if (fileType.equalsIgnoreCase("jar")) {
      baseImage = jdkServerMap.lookupJdkImage(runtimeConfig.getJdk());
    } else if (fileType.equalsIgnoreCase("war")) {
      baseImage = jdkServerMap.lookupServerImage(runtimeConfig.getJdk(), runtimeConfig.getServer());
    } else {
      throw new IllegalArgumentException(
          String.format("Unable to determine the runtime for artifact %s. Expected a .jar or .war "
                  + "file.",
              artifactToDeploy.getFileName()));
    }

    dockerfile.append(String.format(DOCKERFILE, baseImage, artifactToDeploy.toString(), appDest));

    // TODO where does server-specific config like this go?
    //    if (baseImage.equals(serverRuntime) && runtimeConfig.getJettyQuickstart()) {
    //      dockerfile.append("RUN /scripts/jetty/quickstart.sh\n");
    //    }

    return dockerfile.toString();
  }

}
