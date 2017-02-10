package com.google.cloud.runtimes.builder.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.BuildTool;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
  public void testParse_disableRemoteBuild() throws IOException {
    AppYaml resultTrue = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  disable_remote_build: true");
    AppYaml resultFalse = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  disable_remote_build: false");
    assertTrue(resultTrue.getRuntimeConfig().getDisableRemoteBuild());
    assertFalse(resultFalse.getRuntimeConfig().getDisableRemoteBuild());
  }

  @Test
  public void testParse_testBuildTool() throws IOException {
    Map<String, BuildTool> buildTools = new HashMap<>();
    buildTools.put("maven", BuildTool.MAVEN);
    buildTools.put("gradle", BuildTool.GRADLE);

    for (Entry<String, BuildTool> e : buildTools.entrySet()) {
      AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
          + "runtime_config:\n"
          + "  build_tool: " + e.getKey());
      assertEquals(e.getValue(), result.getRuntimeConfig().getBuildTool());
    }
  }

  @Test(expected = InvalidFormatException.class)
  public void testParse_testBuildTool_invalid() throws IOException {
    parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  build_tool: not_a_build_tool");
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
