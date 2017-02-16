package com.google.cloud.runtimes.builder.buildsteps.maven;

import com.google.cloud.runtimes.builder.buildsteps.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.MetadataConstants;
import com.google.cloud.runtimes.builder.exception.BuildStepException;
import com.google.cloud.runtimes.builder.exception.BuildToolInvokerException;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenBuildStep extends BuildStep {

  private final Logger logger = LoggerFactory.getLogger(MavenBuildStep.class);

  private final MavenInvoker mavenInvoker;

  @Inject
  MavenBuildStep(MavenInvoker mavenInvoker) {
    this.mavenInvoker = mavenInvoker;
  }

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {
    Path pomFile = directory.resolve("pom.xml");
    Preconditions.checkArgument(Files.isRegularFile(pomFile));

    // TODO get args from configuration somehow
    try {
//      if (args.isEmpty()) {
        mavenInvoker.invoke(pomFile);
//      } else  {
//        mavenInvoker.invoke(pomFile, args);
//      }

      // TODO query the pom to include overrides
      metadata.put(MetadataConstants.BUILD_ARTIFACT_PATH, "target/");

    } catch (BuildToolInvokerException e) {
      throw new BuildStepException(e);
    }
  }

}
