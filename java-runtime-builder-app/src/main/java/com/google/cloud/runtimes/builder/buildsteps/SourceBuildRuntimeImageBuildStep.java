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
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.inject.Inject;

import java.io.File;

public class SourceBuildRuntimeImageBuildStep extends RuntimeImageBuildStep {

  private final JdkServerLookup jdkServerLookup;

  @Inject
  SourceBuildRuntimeImageBuildStep(JdkServerLookup jdkServerLookup) {
    this.jdkServerLookup = jdkServerLookup;
  }

  @Override
  protected String getArtifact(BuildContext buildContext) {
    String providedArtifactPath = buildContext.getRuntimeConfig().getArtifact();
    if (providedArtifactPath != null) {
      // if the artifact path is set in runtime configuration, use that value
      return providedArtifactPath;
    }

    // otherwise, guess the artifact's location
    // TODO is this even a good idea? error out?
    String dir = buildContext.getBuildArtifactLocation().get().toString();
    String extension = isServerRuntime(buildContext) ? "war" : "jar";
    return dir + File.separator + "*." + extension;
  }

  @Override
  protected String getBaseRuntimeImage(BuildContext buildContext) {
    // TODO throw exception if not set?
    // other option - require that artifact is explicitly set in config, and use its extension to
    // determine the default runtime type. This is more consistent with how it's done for prebuilt
    // artifacts

    RuntimeConfig runtimeConfig = buildContext.getRuntimeConfig();
    String server = runtimeConfig.getServer();
    if (server != null) {
      return jdkServerLookup.lookupServerImage(runtimeConfig.getJdk(), runtimeConfig.getServer());
    } else {
      return jdkServerLookup.lookupJdkImage(runtimeConfig.getJdk());
    }
  }

  private boolean isServerRuntime(BuildContext buildContext) {
    return buildContext.getRuntimeConfig().getServer() != null;
  }
}
