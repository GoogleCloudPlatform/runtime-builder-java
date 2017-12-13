package com.google.cloud.runtimes.builder.injection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.cloud.runtimes.builder.Application;
import com.google.cloud.runtimes.builder.BuildPipelineConfigurator;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.inject.Guice;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
  public void testProvideJdkServerLookupMissingServerDefault() throws IOException {
    String[] jdkMappings = {"*=gcr.io/foo"};
    String[] serverMappings = {"foo=gcr.io/foo"};
    new RootModule(Application.mergeSettingsWithDefaults(serverMappings, jdkMappings), COMPAT_IMAGE,
        MVN_IMAGE, GRADLE_IMAGE,
        DISABLE_BUILD)
        .provideJdkServerLookup();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProvideJdkServerLookupBadArgFormat() throws IOException {
    String[] jdkMappings = {"*=gcr.io/foo"};
    String[] serverMappings = {"foo=gcr.io/foo=bar"};
    new RootModule(Application.mergeSettingsWithDefaults(serverMappings, jdkMappings), COMPAT_IMAGE,
        MVN_IMAGE, GRADLE_IMAGE,
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
        = new RootModule(Application.mergeSettingsWithDefaults(jdkMappings, serverMappings),
        COMPAT_IMAGE, MVN_IMAGE, GRADLE_IMAGE,
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
        new RootModule(Application.mergeSettingsWithDefaults(jdkMappings, serverMappings),
            COMPAT_IMAGE, MVN_IMAGE, GRADLE_IMAGE,
            DISABLE_BUILD))
        .getInstance(BuildPipelineConfigurator.class);
  }

  @Test
  public void testDefaultSettingsNoCommandLineGiven() throws IOException {
    JdkServerLookup jdkServerLookup
        = new RootModule(Application.mergeSettingsWithDefaults(null, null), COMPAT_IMAGE, MVN_IMAGE,
        GRADLE_IMAGE,
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
    String[] serverMappings = {"*|*=gcr.io/server:latest",
        "  openjdk8   | tomcat  = gcr.io/google-appengine/tomcat:8-many-spaces"};
    JdkServerLookup jdkServerLookup
        = new RootModule(Application.mergeSettingsWithDefaults(jdkMappings, serverMappings),
        COMPAT_IMAGE, MVN_IMAGE, GRADLE_IMAGE,
        DISABLE_BUILD, Collections.emptyMap())
        .provideJdkServerLookup();

    // spot-checking some of the default settings
    assertEquals("gcr.io/server:latest", jdkServerLookup.lookupServerImage("*", "*"));
    assertEquals("gcr.io/google-appengine/tomcat:8-many-spaces",
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
  public void testDefaultSettingsMatchJavaYaml()
      throws URISyntaxException, IOException, ParseException {
    Path path = Paths.get("../java.yaml");

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    JavaYaml javaYaml = mapper.readValue(path.toFile(), JavaYaml.class);

    String[] args = ((ArrayList<String>) javaYaml.getSteps()[0].get("args")).toArray(new String[0]);

    Options options = new Options();

    Application.addCliOptions(options);

    CommandLine cmd = new DefaultParser().parse(options, args);

    String[] jdkMappings = cmd.getOptionValues("j");
    String[] serverMappings = cmd.getOptionValues("s");

    assertTrue(Arrays.equals(jdkMappings, Application.DEFAULT_JDK_MAPPINGS));
    assertTrue(Arrays.equals(serverMappings, Application.DEFAULT_SERVER_MAPPINGS));

    String compatImage = cmd.getOptionValue("c");
    String mavenImage = cmd.getOptionValue("m");
    String gradleImage = cmd.getOptionValue("g");

    assertEquals(compatImage, Application.DEFAULT_COMPAT_RUNTIME_IMAGE);
    assertEquals(mavenImage, Application.DEFAULT_MAVEN_DOCKER_IMAGE);
    assertEquals(gradleImage, Application.DEFAULT_GRADLE_DOCKER_IMAGE);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class JavaYaml {

    private Map<String, Object>[] steps;

    public Map<String, Object>[] getSteps() {
      return steps;
    }

    public void setSteps(
        Map<String, Object>[] steps) {
      this.steps = steps;
    }
  }
}
