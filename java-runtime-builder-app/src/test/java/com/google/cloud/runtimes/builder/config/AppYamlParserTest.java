package com.google.cloud.runtimes.builder.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link AppYamlParser}
 */
public class AppYamlParserTest {

  private AppYamlParser appYamlParser;

  private static String APP_YAML_PREAMBLE = "runtime: java\nenv: flex\n";

  @Before
  public void setup() {
    appYamlParser = new AppYamlParser();
  }

  private Path createTempFile(String contents) throws IOException {
    Path path = Files.createTempFile(null, null);
    BufferedWriter writer = Files.newBufferedWriter(path);
    writer.write(contents);
    writer.close();
    return path;
  }

  private AppYaml parseFileWithContents(String contents) throws IOException {
    Path file = createTempFile(contents);
    return appYamlParser.parse(file);
  }

  @Test
  public void testParse_defaultAppYaml() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE);
    assertNull(result.getRuntimeConfig());
  }

  @Test
  public void testParse_emptyRuntimeConfig() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n");
    assertNull(result.getRuntimeConfig());
  }

  @Test
  public void testParse_artifact() throws IOException {
    String artifact = "my/path/to/artifact";
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  artifact: " + artifact);
    assertEquals(artifact, result.getRuntimeConfig().getArtifact());
  }

  @Test
  public void testServer() {
    // TODO
    fail();
  }

  @Test
  public void testJdk() {
    // TODO
    fail();
  }

}
