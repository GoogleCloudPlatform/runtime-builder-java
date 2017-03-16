package com.google.cloud.runtimes.builder.buildsteps.docker;

import com.google.cloud.runtimes.builder.injection.RuntimeDigests;
import com.google.cloud.runtimes.builder.util.FileUtil;
import com.google.inject.Inject;

import java.nio.file.Path;
import java.util.Properties;

/**
 * Default implementation of a {@link DockerfileGenerator}.
 */
public class DefaultDockerfileGenerator implements DockerfileGenerator {

  private static final String DOCKERFILE = "FROM %s\n"
      + "ADD %s %s\n";

  private Properties runtimeDigests;

  /**
   * Constructs a new {@link DefaultDockerfileGenerator}.
   */
  @Inject
  DefaultDockerfileGenerator(@RuntimeDigests Properties runtimeDigests) {
    this.runtimeDigests = runtimeDigests;
  }

  @Override
  public String generateDockerfile(Path artifactToDeploy) {
    String fileType = FileUtil.getFileExtension(artifactToDeploy);
    String runtime;
    String appDest;

    if (fileType.equals("jar")) {
      runtime = "openjdk";
      appDest = "app.jar";
    } else if (fileType.equals("war")) {
      runtime = "jetty";
      appDest = "/app";
    } else {
      throw new IllegalArgumentException(
          String.format("Unable to determine the runtime for artifact %s.",
              artifactToDeploy.getFileName()));
    }

    String baseImage = String.format("gcr.io/google_appengine/%s@%s",
        runtime, runtimeDigests.getProperty(runtime));
    return String.format(DOCKERFILE, baseImage, artifactToDeploy.toString(), appDest);
  }

}
