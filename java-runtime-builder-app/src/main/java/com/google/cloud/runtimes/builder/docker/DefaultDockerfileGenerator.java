package com.google.cloud.runtimes.builder.docker;

import com.google.cloud.runtimes.builder.template.TemplateRenderer;
import com.google.cloud.runtimes.builder.util.FileUtil;

import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Default implementation of a {@link DockerfileGenerator}.
 */
public class DefaultDockerfileGenerator implements DockerfileGenerator {

  // TODO externalize
  private static final String DOCKERFILE = "FROM $BASE_RUNTIME\nADD $APP_NAME /app\n";

  private TemplateRenderer templateRenderer;
  private Properties runtimeDigests;

  /**
   * Constructs a new {@link DefaultDockerfileGenerator}.
   */
  public DefaultDockerfileGenerator(TemplateRenderer templateRenderer, Properties runtimeDigests) {
    this.templateRenderer = templateRenderer;
    this.runtimeDigests = runtimeDigests;
  }

  @Override
  public String generateDockerfile(Path artifactToDeploy) {
    String fileType = FileUtil.getFileExtension(artifactToDeploy);
    String runtime;
    // TODO move to map?
    if (fileType.equals("jar")) {
      runtime = "openjdk";
    } else if (fileType.equals("war")) {
      runtime = "jetty";
    } else {
      throw new IllegalArgumentException("Unable to determine runtime");
    }

    String baseImage = String.format("gcr.io/google_appengine/%s@%s",
        runtime, runtimeDigests.getProperty(runtime));
    Map<String,String> variables = new HashMap<>();
    variables.put("$BASE_RUNTIME", baseImage);
    variables.put("$APP_NAME", artifactToDeploy.toString());
    return templateRenderer.render(DOCKERFILE, variables);
  }

}
