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
 * Build step that invokes an arbitrary string as a command.
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
