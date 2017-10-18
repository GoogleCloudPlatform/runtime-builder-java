package com.google.cloud.runtimes.builder.injection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.runtimes.builder.Application;
import com.google.cloud.runtimes.builder.BuildPipelineConfigurator;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.inject.Guice;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;

import java.io.IOException;

/**
 * Unit tests for {@link RootModule}.
 */
public class RootModuleTest {

  private static final String MVN_IMAGE = "mvn";
  private static final String GRADLE_IMAGE = "gradle";
  private static final String COMPAT_IMAGE = "compat";
  private static final boolean DISABLE_BUILD = false;

  @Test(expected = IllegalArgumentException.class)
  public void testProvideJdkServerLookupMissingJdkDefault() throws IOException {
    String[] jdkMappings = {"foo=gcr.io/foo"};
    String[] serverMappings = {"*|*=gcr.io/foo"};
    new RootModule(jdkMappings, serverMappings, COMPAT_IMAGE, MVN_IMAGE, GRADLE_IMAGE,
        DISABLE_BUILD)
        .provideJdkServerLookup();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProvideJdkServerLookupMissingServerDefault() throws IOException {
    String[] jdkMappings = {"*=gcr.io/foo"};
    String[] serverMappings = {"foo=gcr.io/foo"};
    new RootModule(jdkMappings, serverMappings, COMPAT_IMAGE, MVN_IMAGE, GRADLE_IMAGE,
        DISABLE_BUILD)
        .provideJdkServerLookup();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProvideJdkServerLookupBadArgFormat() throws IOException {
    String[] jdkMappings = {"*=gcr.io/foo"};
    String[] serverMappings = {"foo=gcr.io/foo=bar"};
    new RootModule(jdkMappings, serverMappings, COMPAT_IMAGE, MVN_IMAGE, GRADLE_IMAGE,
        DISABLE_BUILD)
        .provideJdkServerLookup();
  }

  @Test
  public void testProvideJdkServerLookup() throws IOException {
    String[] jdkMappings = {
        "*=gcr.io/jdk:latest",
        "someJdk=gcr.io/jdk:other"
    };
    String[] serverMappings = {
        "*|*=gcr.io/server:latest",
        "key1|*=gcr.io/server:version"
    };

    JdkServerLookup jdkServerLookup
        = new RootModule(jdkMappings, serverMappings, COMPAT_IMAGE, MVN_IMAGE, GRADLE_IMAGE,
        DISABLE_BUILD)
        .provideJdkServerLookup();

    assertEquals("gcr.io/jdk:latest", jdkServerLookup.lookupJdkImage(null));
    assertEquals("gcr.io/jdk:other", jdkServerLookup.lookupJdkImage("someJdk"));
    assertEquals("gcr.io/server:latest", jdkServerLookup.lookupServerImage(null, null));
    assertEquals("gcr.io/server:version", jdkServerLookup.lookupServerImage("key1", null));
  }

  @Test
  public void testConfigure() {
    String[] jdkMappings = {"*=gcr.io/jdk:latest"};
    String[] serverMappings = {"*|*=gcr.io/server:latest"};
    // test that the bindings can be created without errors
    Guice.createInjector(
        new RootModule(jdkMappings, serverMappings, COMPAT_IMAGE, MVN_IMAGE, GRADLE_IMAGE,
            DISABLE_BUILD))
        .getInstance(BuildPipelineConfigurator.class);
  }

  @Test
  public void testDefaultSettingsNoCommandLineGiven() throws IOException {
    JdkServerLookup jdkServerLookup
        = new RootModule(null, Application.getDefaultJdkMappings(), null,
        Application.getDefaultServerMappings(), COMPAT_IMAGE, MVN_IMAGE, GRADLE_IMAGE,
        DISABLE_BUILD, Collections.emptyMap())
        .provideJdkServerLookup();

    // spot-checking some of the default settings
    assertEquals("gcr.io/google-appengine/jetty:9", jdkServerLookup.lookupServerImage("*", "*"));
    assertEquals("gcr.io/google-appengine/tomcat:8",
        jdkServerLookup.lookupServerImage("openjdk8", "tomcat"));
    assertEquals("gcr.io/google-appengine/tomcat:latest",
        jdkServerLookup.lookupServerImage("*", "tomcat"));

    assertEquals("gcr.io/google-appengine/openjdk:8", jdkServerLookup.lookupJdkImage("*"));
    assertEquals("gcr.io/google-appengine/openjdk:8", jdkServerLookup.lookupJdkImage("openjdk8"));
    assertEquals("gcr.io/google-appengine/openjdk:9", jdkServerLookup.lookupJdkImage("openjdk9"));
  }

  @Test
  public void testDefaultSettingsPartialCommandLineGiven() throws IOException {
    String[] jdkMappings = {"*=gcr.io/jdk:latest"};
    String[] serverMappings = {"*|*=gcr.io/server:latest"};
    JdkServerLookup jdkServerLookup
        = new RootModule(jdkMappings, Application.getDefaultJdkMappings(), serverMappings,
        Application.getDefaultServerMappings(), COMPAT_IMAGE, MVN_IMAGE, GRADLE_IMAGE,
        DISABLE_BUILD, Collections.emptyMap())
        .provideJdkServerLookup();

    // spot-checking some of the default settings
    assertEquals("gcr.io/server:latest", jdkServerLookup.lookupServerImage("*", "*"));
    assertEquals("gcr.io/google-appengine/tomcat:8",
        jdkServerLookup.lookupServerImage("openjdk8", "tomcat"));
    assertEquals("gcr.io/google-appengine/tomcat:latest",
        jdkServerLookup.lookupServerImage("*", "tomcat"));

    assertEquals("gcr.io/jdk:latest", jdkServerLookup.lookupJdkImage("*"));
    assertEquals("gcr.io/google-appengine/openjdk:8", jdkServerLookup.lookupJdkImage("openjdk8"));
    assertEquals("gcr.io/google-appengine/openjdk:9", jdkServerLookup.lookupJdkImage("openjdk9"));
  }

  /**
   * A check that the default settings are not outdated compared to what is in java.yaml.
   */
  @Test
  public void testDefaultSettingsMatchJavaYaml() throws URISyntaxException, IOException {
    Path path = Paths.get("../java.yaml");
    String javaYaml = new String(Files.readAllBytes(path));

    // A simple check to see that all of the default settings appear in java.yaml and are therefore
    // not outdated. There might still be settings in java.yaml that aren't in the default settings.
    // Intentionally not parsing java.yaml to not depend on its structure.
    Map<String, String> defaultJdk = Application.getDefaultJdkMappings();
    for (String key : defaultJdk.keySet()) {
      assertTrue(javaYaml.contains(key + "=" + defaultJdk.get(key)));
    }
    Map<String, String> defaultServer = Application.getDefaultServerMappings();
    for (String key : defaultServer.keySet()) {
      assertTrue(javaYaml.contains(key + "=" + defaultServer.get(key)));
    }
  }
}
