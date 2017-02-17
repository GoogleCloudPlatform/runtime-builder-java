package com.google.cloud.runtimes.builder.buildsteps.maven;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepMetadataConstants;
import com.google.cloud.runtimes.builder.exception.BuildToolInvokerException;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenBuildStep extends BuildStep {

  private final static Logger logger = LoggerFactory.getLogger(MavenBuildStep.class);
  private final MavenInvoker mavenInvoker;

  public static void main(String[] args) throws IOException, BuildStepException {
    logger.info("Running main method");
    new MavenBuildStep(Arrays.asList(args)).run(Paths.get(System.getProperty("user.dir")));
  }

  public MavenBuildStep(List<String> args) {
    // TODO delete this constructor
    mavenInvoker = new MavenInvoker();
  }

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
      metadata.put(BuildStepMetadataConstants.BUILD_ARTIFACT_PATH, "target/");

    } catch (BuildToolInvokerException e) {
      throw new BuildStepException(e);
    }
  }

}
