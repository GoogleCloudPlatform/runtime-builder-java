package com.google.cloud.runtimes.builder.buildsteps.docker;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link DefaultDockerfileGenerator}.
 */
public class DefaultDockerfileGeneratorTest {

  private DefaultDockerfileGenerator generator;
  @Mock private Properties runtimeDigestsProperties;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    generator = new DefaultDockerfileGenerator(runtimeDigestsProperties);
  }

  @Test
  public void testGenerateOpenjdk() throws IOException {
    Path jar = Files.createTempFile( null, ".jar").toAbsolutePath();
    testGenerateForBaseRuntime("openjdk", jar);
  }

  @Test
  public void testGenerateJetty() throws IOException {
    Path war = Files.createTempFile( null, ".war").toAbsolutePath();
    testGenerateForBaseRuntime("jetty", war);
  }

  private void testGenerateForBaseRuntime(String runtimeName, Path artifactToDeploy) {
    String digest = "sha256:123456789";

    when(runtimeDigestsProperties.getProperty(runtimeName)).thenReturn(digest);
    String result = generator.generateDockerfile(artifactToDeploy);
    assertTrue(result.contains("FROM gcr.io/google_appengine/" + runtimeName + "@" + digest));
    assertTrue(result.contains("ADD " + artifactToDeploy.toString()));
  }

}
