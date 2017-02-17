package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    for (BuildStep buildStep : buildSteps) {
      buildStep.run(workspaceDir);
    }
  }
}
