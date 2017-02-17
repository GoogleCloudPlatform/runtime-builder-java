package com.google.cloud.runtimes.builder.buildsteps.maven;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepMetadataConstants;
import com.google.cloud.runtimes.builder.exception.BuildToolInvokerException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class MavenBuildStep extends BuildStep {

  private static final List<String> DEFAULT_GOALS
      = ImmutableList.of("-B", "-DskipTests=true", "clean", "package");

  private static final Logger logger = LoggerFactory.getLogger(MavenBuildStep.class);
  private final MavenInvoker mavenInvoker;

  @Inject
  MavenBuildStep(MavenInvoker mavenInvoker) {
    this.mavenInvoker = mavenInvoker;
  }

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {
    Path pomFile = directory.resolve("pom.xml");
    Preconditions.checkArgument(Files.isRegularFile(pomFile));

    logger.info("Invoking maven build step with goals {}", DEFAULT_GOALS);

    try {
      mavenInvoker.invoke(pomFile, DEFAULT_GOALS);

      // TODO query the pom to include overrides
      metadata.put(BuildStepMetadataConstants.BUILD_ARTIFACT_PATH, "target/");

    } catch (BuildToolInvokerException e) {
      throw new BuildStepException(e);
    }
  }

}
