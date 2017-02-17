package com.google.cloud.runtimes.builder.buildsteps.gradle;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class GradleBuildStep extends BuildStep {

  GradleBuildStep() {
  }

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {
    // TODO

  }
}
