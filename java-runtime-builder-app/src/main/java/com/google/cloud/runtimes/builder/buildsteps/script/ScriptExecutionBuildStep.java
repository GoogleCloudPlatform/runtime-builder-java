package com.google.cloud.runtimes.builder.buildsteps.script;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.nio.file.Path;
import java.util.Map;

public class ScriptExecutionBuildStep extends BuildStep {

  @Inject
  ScriptExecutionBuildStep(@Assisted String buildCommand) {
  }

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {

  }
}
