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

package com.google.cloud.runtimes.builder.buildsteps.script;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Build step that invokes an arbitrary string as a shell command.
 */
public class ScriptExecutionBuildStep extends BuildStep {

  private final Logger logger = LoggerFactory.getLogger(ScriptExecutionBuildStep.class);
  private final String buildCommand;

  @Inject
  ScriptExecutionBuildStep(@Assisted String buildCommand) {
    this.buildCommand = buildCommand;
  }

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {
    try {
      logger.info("Executing build command '{}' in directory {}", buildCommand, directory);
      new ProcessBuilder()
          .command(buildCommand.split("\\s+"))
          .directory(directory.toFile())
          .inheritIO()
          .start().waitFor();
    } catch (IOException | InterruptedException e) {
      throw new BuildStepException(e);
    }

  }
}
