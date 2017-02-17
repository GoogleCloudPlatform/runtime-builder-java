package com.google.cloud.runtimes.builder.buildsteps;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import java.nio.file.Path;
import java.util.Map;

public class ScriptExecutionBuildStep extends BuildStep {

  public ScriptExecutionBuildStep(String buildCommand) {
  }

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {

  }
}
