/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

import com.google.cloud.runtimes.builder.injection.RuntimeDigests;
import com.google.cloud.runtimes.builder.util.FileUtil;
import com.google.inject.Inject;

import java.nio.file.Path;
import java.util.Properties;

/**
 * Default implementation of a {@link DockerfileGenerator}.
 */
public class DefaultDockerfileGenerator implements DockerfileGenerator {

  private static final String DOCKERFILE = "FROM %s\n"
      + "ADD %s %s\n";

  private Properties runtimeDigests;

  /**
   * Constructs a new {@link DefaultDockerfileGenerator}.
   */
  @Inject
  DefaultDockerfileGenerator(@RuntimeDigests Properties runtimeDigests) {
    this.runtimeDigests = runtimeDigests;
  }

  @Override
  public String generateDockerfile(Path artifactToDeploy) {
    String fileType = FileUtil.getFileExtension(artifactToDeploy);
    String runtime;
    String appDest;

    if (fileType.equals("jar")) {
      runtime = "openjdk";
      appDest = "app.jar";
    } else if (fileType.equals("war")) {
      runtime = "jetty";
      appDest = "/app";
    } else {
      throw new IllegalArgumentException(
          String.format("Unable to determine the runtime for artifact %s.",
              artifactToDeploy.getFileName()));
    }

    String baseImage = String.format("gcr.io/google_appengine/%s@%s",
        runtime, runtimeDigests.getProperty(runtime));
    return String.format(DOCKERFILE, baseImage, artifactToDeploy.toString(), appDest);
  }

}
