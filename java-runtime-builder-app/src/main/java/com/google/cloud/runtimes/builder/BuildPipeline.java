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

package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Encapsulates a set of arbitrary transformations that are executed on a directory in series.
 */
public class BuildPipeline {

  private final Logger logger = LoggerFactory.getLogger(BuildPipeline.class);
  private final BuildPipelineConfigurator buildPipelineConfigurator;

  /**
   * Constructs a new {@link BuildPipeline}.
   */
  @Inject
  public BuildPipeline(BuildPipelineConfigurator buildPipelineConfigurator) {
    this.buildPipelineConfigurator = buildPipelineConfigurator;
  }

  /**
   * Examines the workspace directory and executes the required set of build steps based on its
   * contents.
   *
   * @throws AppYamlNotFoundException if an app.yaml config file is not found in the directory
   * @throws IOException if a transient file system error was encountered
   * @throws BuildStepException if one of the build steps encountered an error
   */
  public void build(Path workspaceDir) throws AppYamlNotFoundException, IOException,
      BuildStepException {
    List<BuildStep> buildSteps = buildPipelineConfigurator.configurePipeline(workspaceDir);
    logger.info("Beginning build pipeline {}", buildSteps);
    for (BuildStep buildStep : buildSteps) {
      logger.info("Executing build step {}", buildStep);
      buildStep.run(workspaceDir);
    }
  }
}
