package com.google.cloud.runtimes.builder.buildsteps.docker;

import com.google.cloud.runtimes.builder.injection.RuntimeDigests;
import com.google.cloud.runtimes.builder.template.TemplateRenderer;
import com.google.cloud.runtimes.builder.util.FileUtil;
import com.google.inject.Inject;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Default implementation of a {@link DockerfileGenerator}.
 */
public class DefaultDockerfileGenerator implements DockerfileGenerator {

  private static final String DOCKERFILE = "FROM $BASE_RUNTIME\n"
      + "ADD $APP_NAME $APP_DEST\n";

  private TemplateRenderer templateRenderer;
  private Properties runtimeDigests;

  /**
   * Constructs a new {@link DefaultDockerfileGenerator}.
   */
  @Inject
  DefaultDockerfileGenerator(TemplateRenderer templateRenderer,
      @RuntimeDigests Properties runtimeDigests) {
    this.templateRenderer = templateRenderer;
    this.runtimeDigests = runtimeDigests;
  }

  @Override
  public String generateDockerfile(Path artifactToDeploy) {
    String fileType = FileUtil.getFileExtension(artifactToDeploy);
    String runtime;
    String appDest;
    // TODO factor in the user's overrides in runtime_config. consider how to support unrecognized
    // file extensions
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
    Map<String,String> variables = new HashMap<>();
    variables.put("$BASE_RUNTIME", baseImage);
    variables.put("$APP_NAME", artifactToDeploy.toString());
    variables.put("$APP_DEST", appDest);
    return templateRenderer.render(DOCKERFILE, variables);
  }

}
