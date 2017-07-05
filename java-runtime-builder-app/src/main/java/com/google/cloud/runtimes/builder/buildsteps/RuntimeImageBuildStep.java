package com.google.cloud.runtimes.builder.buildsteps;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.exception.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.exception.TooManyArtifactsException;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class RuntimeImageBuildStep implements BuildStep {

  private final Logger logger = LoggerFactory.getLogger(RuntimeImageBuildStep.class);
  private final JdkServerLookup jdkServerLookup;

  @Inject
  RuntimeImageBuildStep(JdkServerLookup jdkServerLookup) {
    this.jdkServerLookup = jdkServerLookup;
  }

  @Override
  public void run(BuildContext buildContext) throws BuildStepException {
    buildContext.getDockerfile().append("FROM "
        + getBaseRuntimeImage(buildContext.getRuntimeConfig()));
    buildContext.getDockerfile().append("ADD " + getArtifact(buildContext) + " $APP_DESTINATION");
  }

  private String getBaseRuntimeImage(RuntimeConfig runtimeConfig) {
    String server = runtimeConfig.getServer();
    if (server != null) {
      return jdkServerLookup.lookupServerImage(runtimeConfig.getJdk(), runtimeConfig.getServer());
    } else {
      return jdkServerLookup.lookupJdkImage(runtimeConfig.getJdk());
    }
  }

  /*
   * Returns the artifact to use for staging.
   */
  private Path getArtifact(BuildContext buildContext) throws BuildStepException {
    String configuredArtifactPath = buildContext.getRuntimeConfig().getArtifact();

    // TODO if this is a prebuilt case, search for it. otherwise, use some default like *.jar, etc.
    // directory.toString() + File.separator + "*.(war|jar)";

    if (configuredArtifactPath != null) {
      // if the artifact path is set in runtime configuration, use that value
      return buildContext.getWorkspaceDir().resolve(configuredArtifactPath);
    } else if (buildContext.getExpectedArtifactDir().isPresent()) {
      // if the artifact path was set by other parts of the build
      return searchForArtifactInDir(buildContext.getWorkspaceDir().resolve(
          buildContext.getExpectedArtifactDir().get()));
    } else {
      // otherwise, search for an artifact in the workspace root
      return searchForArtifactInDir(buildContext.getWorkspaceDir());
    }
  }

  /*
   * Searches for files that look like deployable artifacts in the given directory
   */
  private Path searchForArtifactInDir(Path directory) throws BuildStepException {
    logger.info("Searching for a deployable artifact in {}", directory.toString());
    if (!Files.isDirectory(directory)) {
      throw new IllegalArgumentException(String.format("%s is not a valid directory.", directory));
    }

    List<Path> validArtifacts = null;
    try {
      validArtifacts = Files.list(directory)
          // filter out files that don't end in .war or .jar
          .filter((path) -> {
            String extension = com.google.common.io.Files.getFileExtension(path.toString());
            return extension.equals("war") || extension.equals("jar");
          })
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new BuildStepException("An error was encountered while searching for deployable "
          + "artifacts.", e);
    }

    if (validArtifacts.size() < 1) {
      throw new ArtifactNotFoundException();
    } else if (validArtifacts.size() > 1) {
      throw new TooManyArtifactsException(validArtifacts);
    } else {
      return validArtifacts.get(0);
    }
  }
}
