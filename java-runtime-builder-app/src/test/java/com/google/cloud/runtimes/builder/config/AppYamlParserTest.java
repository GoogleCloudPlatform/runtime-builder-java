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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.Application;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;

import com.google.cloud.runtimes.builder.config.domain.BetaSettings;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
  }

  @Test
  public void testParseEnableAppEngineApisFalseWithoutCmdSetting() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "beta_settings:\n"
        + "  enable_app_engine_apis: false");
    assertFalse(result.getBetaSettings().isEnableAppEngineApis());
  }

  @Test
  public void testParseEnableAppEngineApisTrueWithCmdSetting() throws IOException {
    Map<String, Object> overrideSettings = new HashMap<>();
    overrideSettings.put("enable_app_engine_apis", true);
    appYamlParser = new AppYamlParser();
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "beta_settings:\n"
        + "  enable_app_engine_apis: false");
    result.applyOverrideSettings(overrideSettings);
    assertTrue(result.getBetaSettings().isEnableAppEngineApis());
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
  public void testParseEnableAppEngineApisTrueWithoutYaml() throws Exception {
    Map<String, Object> overrideSettings = new HashMap<>();
    overrideSettings.put("enable_app_engine_apis", true);
    appYamlParser = new AppYamlParser();
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE);
    result.applyOverrideSettings(overrideSettings);
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
  public void testParseArtifactWithCmdSetting() throws IOException {
    String artifact = "my/path/to/artifact";
    String otherArtifact = "my/path/to/other_artifact";

    Map<String, Object> overrideSettings = new HashMap<>();
    overrideSettings.put("artifact", otherArtifact);
    appYamlParser = new AppYamlParser();

    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  artifact: " + artifact);
    result.applyOverrideSettings(overrideSettings);
    assertEquals(otherArtifact, result.getRuntimeConfig().getArtifact());
  }

  @Test
  public void testParseJdkWithCmdSetting() throws IOException {
    String jdk = "jdk";
    String otherJdk = "other_jdk";

    Map<String, Object> overrideSettings = new HashMap<>();
    overrideSettings.put("jdk", otherJdk);
    appYamlParser = new AppYamlParser();

    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  jdk: " + jdk);
    result.applyOverrideSettings(overrideSettings);
    assertEquals(otherJdk, result.getRuntimeConfig().getJdk());
  }

  @Test
  public void testParseBuildScriptWithCmdSetting() throws IOException {
    String buildScript = "build_script";
    String otherBuildScript = "other_build_script";

    Map<String, Object> overrideSettings = new HashMap<>();
    overrideSettings.put("build_script", otherBuildScript);
    appYamlParser = new AppYamlParser();

    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  build_script: " + buildScript);
    result.applyOverrideSettings(overrideSettings);
    assertEquals(otherBuildScript, result.getRuntimeConfig().getBuildScript());
  }

  @Test
  public void testParseArtifactWithoutYaml() throws IOException {
    String otherArtifact = "my/path/to/other_artifact";

    Map<String, Object> overrideSettings = new HashMap<>();
    overrideSettings.put("artifact", otherArtifact);
    appYamlParser = new AppYamlParser();

    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE);
    result.applyOverrideSettings(overrideSettings);
    assertEquals(otherArtifact, result.getRuntimeConfig().getArtifact());
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
  public void testSetRuntimeConfig() {
    AppYaml result = new AppYaml();
    RuntimeConfig config = new RuntimeConfig();
    result.setRuntimeConfig(config);
    assertTrue(result.getRuntimeConfig() == config);
  }

  @Test
  public void testSetBetaSettings() {
    AppYaml result = new AppYaml();
    BetaSettings config = new BetaSettings();
    result.setBetaSettings(config);
    assertTrue(result.getBetaSettings() == config);
  }

  @Test
  public void testGetAppYamlOverrideSettings() {
    CommandLine cmd = mock(CommandLine.class);
    String jdkKey = "jdk";
    String jdkValue = "jdkSetting";
    String jettyQuickstartKey = "jetty_quickstart";
    when(cmd.getOptionValue(jdkKey)).thenReturn(jdkValue);
    when(cmd.hasOption(jettyQuickstartKey)).thenReturn(true);

    Map<String, Object> result = Application.getAppYamlOverrideSettings(cmd);
    assertEquals(2, result.size());
    assertEquals(jdkValue, result.get(jdkKey));
    assertEquals(true, result.get(jettyQuickstartKey));
  }

  @Test
  public void testAddOverrideSettingsToOptions() {
    Options options = new Options();
    Application.addOverrideSettingsToOptions(options);

    assertFalse(options.getOption("enable_app_engine_apis").hasArg());
    assertEquals("Replaces the setting from app.yaml under beta_settings : enable_app_engine_apis",
        options.getOption("enable_app_engine_apis").getDescription());

    assertFalse(options.getOption("jetty_quickstart").hasArg());
    assertEquals("Replaces the setting from app.yaml under runtime_config : jetty_quickstart",
        options.getOption("jetty_quickstart").getDescription());

    assertTrue(options.getOption("build_script").hasArg());
    assertEquals("Replaces the setting from app.yaml under runtime_config : build_script",
        options.getOption("build_script").getDescription());

    assertTrue(options.getOption("jdk").hasArg());
    assertEquals("Replaces the setting from app.yaml under runtime_config : jdk",
        options.getOption("jdk").getDescription());

    assertTrue(options.getOption("server").hasArg());
    assertEquals("Replaces the setting from app.yaml under runtime_config : server",
        options.getOption("server").getDescription());

    assertTrue(options.getOption("artifact").hasArg());
    assertEquals("Replaces the setting from app.yaml under runtime_config : artifact",
        options.getOption("artifact").getDescription());
  }
}
