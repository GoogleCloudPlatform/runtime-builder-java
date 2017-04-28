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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Build step that invokes an arbitrary string as a shell command.
 */
public abstract class AbstractSubprocessBuildStep extends BuildStep {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * Returns the build command that is executed by a subprocess in this build step.
   */
  protected abstract List<String> getBuildCommand(Path buildDirectory);

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {
    try {
      List<String> buildCommand = getBuildCommand(directory);

      logger.info("Executing build command '{}' in directory {}", buildCommand, directory);

      int exitCode = new ProcessBuilder()
          .command(buildCommand)
          .directory(directory.toFile())
          .inheritIO()
          .start().waitFor();

      if (exitCode != 0) {
        throw new BuildStepException(
            String.format("Child process exited with non-zero exit code: %s", exitCode));
      }

    } catch (IOException | InterruptedException e) {
      throw new BuildStepException(e);
    }

  }
}
