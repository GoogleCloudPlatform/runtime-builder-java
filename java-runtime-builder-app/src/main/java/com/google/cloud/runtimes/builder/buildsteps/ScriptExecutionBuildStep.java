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

import com.google.cloud.runtimes.builder.buildsteps.base.AbstractSubprocessBuildStep;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Build step that invokes an arbitrary string as a shell command.
 */
public class ScriptExecutionBuildStep extends AbstractSubprocessBuildStep {

  private final String buildCommand;

  @Inject
  ScriptExecutionBuildStep(@Assisted String buildCommand) {
    this.buildCommand = buildCommand;
  }

  @Override
  protected List<String> getBuildCommand(Path buildDirectory) {
    return Arrays.asList("/bin/bash", "-c", buildCommand);
  }

}
