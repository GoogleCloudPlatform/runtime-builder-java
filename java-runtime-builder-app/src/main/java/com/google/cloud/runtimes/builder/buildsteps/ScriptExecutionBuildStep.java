package com.google.cloud.runtimes.builder.buildsteps;

import com.google.cloud.runtimes.builder.exception.BuildStepException;
import java.nio.file.Path;
import java.util.Map;

public class ScriptExecutionBuildStep extends BuildStep {

  public ScriptExecutionBuildStep(String buildCommand) {
  }

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {

  }
}
