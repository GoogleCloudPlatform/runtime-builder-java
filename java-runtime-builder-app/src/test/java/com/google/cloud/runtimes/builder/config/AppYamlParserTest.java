/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.runtimes.builder.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.runtimes.builder.config.domain.AppYaml;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    assertNotNull(result.getRuntimeConfig());
    assertNotNull(result.getBetaSettings());

    assertNull(result.getRuntimeConfig().getServer());
    assertFalse(result.getRuntimeConfig().getJettyQuickstart());
    assertNull(result.getRuntimeConfig().getBuildScript());
    assertNull(result.getRuntimeConfig().getArtifact());
    assertNull(result.getRuntimeConfig().getJdk());

    assertFalse(result.getBetaSettings().isEnableAppEngineApis());

    assertFalse(result.getVm());
  }

  @Test
  public void testParseEnableAppEngineApisFalse() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "beta_settings:\n"
        + "  enable_app_engine_apis: false");
    assertFalse(result.getBetaSettings().isEnableAppEngineApis());
  }

  @Test
  public void testParseEnableAppEngineApisUpperFalse() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "beta_settings:\n"
        + "  enable_app_engine_apis: False");
    assertFalse(result.getBetaSettings().isEnableAppEngineApis());
  }

  @Test
  public void testParseEnableAppEngineApisTrue() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "beta_settings:\n"
        + "  enable_app_engine_apis: true");
    assertTrue(result.getBetaSettings().isEnableAppEngineApis());
  }

  @Test
  public void testParseEnableAppEngineApisUpperTrue() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "beta_settings:\n"
        + "  enable_app_engine_apis: True");
    assertTrue(result.getBetaSettings().isEnableAppEngineApis());
  }

  @Test
  public void testParseEmptyBetaSettings() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "beta_settings:\n");
    assertFalse(result.getBetaSettings().isEnableAppEngineApis());
  }

  @Test
  public void testParse_emptyRuntimeConfig() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n");
    assertNotNull(result.getRuntimeConfig());
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
  public void testParseJettyQuickstart() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  jetty_quickstart: true");
    assertTrue(result.getRuntimeConfig().getJettyQuickstart());
  }

  @Test(expected = com.fasterxml.jackson.databind.exc.InvalidFormatException.class)
  public void testParseInvalidJettyQuickstart() throws IOException {
    parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  jetty_quickstart: invalid_quickstart_option");
  }

  @Test
  public void testParseServer() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
      + "runtime_config:\n"
      + "  server: \"tomcat\"");
    assertTrue(result.getRuntimeConfig().getServer().equals("tomcat"));
  }

  @Test
  public void testParseVmEmpty() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE);
    assertFalse(result.getVm());
  }

  @Test
  public void testParseVmDefault() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE);
    assertFalse(result.getVm());
  }

  @Test
  public void testParseVmTrue() throws IOException {
    AppYaml result = parseFileWithContents("vm: true");
    assertTrue(result.getVm());
  }

  @Test
  public void testParseVmFalse() throws IOException {
    AppYaml result = parseFileWithContents("vm: False");
    assertFalse(result.getVm());
  }

}
