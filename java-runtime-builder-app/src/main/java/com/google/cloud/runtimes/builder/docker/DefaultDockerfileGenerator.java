package com.google.cloud.runtimes.builder.docker;

import com.google.cloud.runtimes.builder.template.TemplateRenderer;
import com.google.cloud.runtimes.builder.util.FileUtil;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DefaultDockerfileGenerator implements DockerfileGenerator {

  // TODO externalize
  private static final String DOCKERFILE = "FROM $BASE_RUNTIME\nADD $APP_NAME /app\n";

  private TemplateRenderer templateRenderer;
  private Properties runtimeDigests;

  @Inject
  public DefaultDockerfileGenerator(TemplateRenderer templateRenderer, Properties runtimeDigests) {
    this.templateRenderer = templateRenderer;
    this.runtimeDigests = runtimeDigests;
  }

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

    String baseImage = String.format("gcr.io/google_appengine/%s@%s", runtime, runtimeDigests.getProperty(runtime));
    Map<String,String> variables = new HashMap<>();
    variables.put("$BASE_RUNTIME", baseImage);
    variables.put("$APP_NAME", artifactToDeploy.toString());
    return templateRenderer.render(DOCKERFILE, variables);
  }

}
