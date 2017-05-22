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

import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.injection.JarRuntimeImage;
import com.google.cloud.runtimes.builder.injection.ServerRuntimeImage;
import com.google.common.io.Files;
import com.google.inject.Inject;

import java.nio.file.Path;

/**
 * Default implementation of a {@link DockerfileGenerator}.
 */
public class DefaultDockerfileGenerator implements DockerfileGenerator {

  private static final String DOCKERFILE = "FROM %s\n"
      + "ADD %s %s\n";

  private final String jarRuntime;
  private final String serverRuntime;

  /**
   * Constructs a new {@link DefaultDockerfileGenerator}.
   */
  @Inject
  DefaultDockerfileGenerator(@JarRuntimeImage String jarRuntime,
      @ServerRuntimeImage String serverRuntime) {
    this.jarRuntime = jarRuntime;
    this.serverRuntime = serverRuntime;
  }

  @Override
  public String generateDockerfile(Path artifactToDeploy, RuntimeConfig runtimeConfig) {
    StringBuilder dockerfile = new StringBuilder();
    String fileType = Files.getFileExtension(artifactToDeploy.toString());
    String baseImage;
    String appDest;

    if (fileType.equals("jar")) {
      baseImage = jarRuntime;
      appDest = "app.jar";
    } else if (fileType.equals("war")) {
      baseImage = serverRuntime;
      appDest = "$JETTY_BASE/webapps/root.war";
    } else {
      throw new IllegalArgumentException(
          String.format("Unable to determine the runtime for artifact %s.",
              artifactToDeploy.getFileName()));
    }
    dockerfile.append(String.format(DOCKERFILE, baseImage, artifactToDeploy.toString(), appDest));

    if (baseImage.equals(serverRuntime) && runtimeConfig.getJettyQuickstart()) {
      dockerfile.append("RUN /scripts/jetty/quickstart.sh");
    }

    return dockerfile.toString();
  }

}
