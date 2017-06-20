package com.google.cloud.runtimes.builder.injection;

import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import org.junit.Test;

public class RootModuleTest {

  @Test
  public void testProvideRuntimeLookup() {

    String defaultJdk = "jdk-1";
    String map = "{"
        + "\"jdk-1\": {"
          + "\"jdk_image\": \"docker-image-jdk-1\","
          + "\"default_server\": \"jetty9\","
          + "\"server_images\": {"
              + "\"jetty9\": \"gcr.io/google_appengine/jetty:9\","
              + "\"tomcat8.5\": \"gcr.io/google_appengine/tomcat:8.5\""
            + "}"
          + "}"
        + "}";

    RootModule module = new RootModule(map, defaultJdk);
    JdkServerLookup result = module.provideJdkServerLookup();
  }
}
