package com.google.cloud.runtimes.builder.injection;

import static org.junit.Assert.assertEquals;

import com.google.cloud.runtimes.builder.BuildPipelineConfigurator;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.inject.Guice;

import org.junit.Test;

import java.io.IOException;

/**
 * Unit tests for {@link RootModule}.
 */
public class RootModuleTest {

  private static final String MVN_IMAGE = "mvn";
  private static final String GRADLE_IMAGE = "gradle";

  @Test(expected = IllegalArgumentException.class)
  public void testProvideJdkServerLookupMissingJdkDefault() throws IOException {
    String[] jdkMappings = {"foo=gcr.io/foo"};
    String[] serverMappings = {"*|*=gcr.io/foo"};
    new RootModule(jdkMappings, serverMappings, MVN_IMAGE, GRADLE_IMAGE).provideJdkServerLookup();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProvideJdkServerLookupMissingServerDefault() throws IOException {
    String[] jdkMappings = {"*=gcr.io/foo"};
    String[] serverMappings = {"foo=gcr.io/foo"};
    new RootModule(jdkMappings, serverMappings, MVN_IMAGE, GRADLE_IMAGE).provideJdkServerLookup();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProvideJdkServerLookupBadArgFormat() throws IOException {
    String[] jdkMappings = {"*=gcr.io/foo"};
    String[] serverMappings = {"foo=gcr.io/foo=bar"};
    new RootModule(jdkMappings, serverMappings, MVN_IMAGE, GRADLE_IMAGE).provideJdkServerLookup();
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
        = new RootModule(jdkMappings, serverMappings, MVN_IMAGE, GRADLE_IMAGE)
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
    Guice.createInjector(new RootModule(jdkMappings, serverMappings, MVN_IMAGE, GRADLE_IMAGE))
        .getInstance(BuildPipelineConfigurator.class);
  }

}
