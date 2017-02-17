package com.google.cloud.runtimes.builder.buildsteps.gradle;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class GradleBuildStep extends BuildStep {

  private final List<String> args;

  public GradleBuildStep(List<String> args) {
    this.args = args;
  }

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {
    // TODO

  }
}
