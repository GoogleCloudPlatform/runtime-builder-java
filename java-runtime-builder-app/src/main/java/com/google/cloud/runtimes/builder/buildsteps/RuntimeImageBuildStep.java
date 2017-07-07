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

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;

public abstract class RuntimeImageBuildStep implements BuildStep {

  @Override
  public void run(BuildContext buildContext) throws BuildStepException {
    buildContext.getDockerfile().append("FROM " + getBaseRuntimeImage(buildContext) + "\n");
    buildContext.getDockerfile().append("ADD " + getArtifact(buildContext) + " $APP_DESTINATION\n");
  }

  /**
   * Returns the name of the base image runtime.
   */
  protected abstract String getBaseRuntimeImage(BuildContext buildContext)
      throws BuildStepException;

  /**
   * Returns the artifact to add to the runtime docker image.
   */
  protected abstract String getArtifact(BuildContext buildContext) throws BuildStepException;

}
