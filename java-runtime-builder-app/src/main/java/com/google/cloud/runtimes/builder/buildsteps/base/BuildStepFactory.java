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

package com.google.cloud.runtimes.builder.buildsteps.base;

import com.google.cloud.runtimes.builder.buildsteps.GradleBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.MavenBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.ScriptExecutionBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.docker.StageDockerArtifactBuildStep;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

/**
 * Factory interface to simplify instantiation of objects with Guice-provided dependencies. See
 * <a href="https://github.com/google/guice/wiki/AssistedInject#assistedinject-in-guice-30">docs</a>
 *  for more.
 */
public interface BuildStepFactory {

  MavenBuildStep createMavenBuildStep();

  GradleBuildStep createGradleBuildStep();

  StageDockerArtifactBuildStep createStageDockerArtifactBuildStep(RuntimeConfig runtimeConfig);

  ScriptExecutionBuildStep createScriptExecutionBuildStep(String buildCommand);

}
