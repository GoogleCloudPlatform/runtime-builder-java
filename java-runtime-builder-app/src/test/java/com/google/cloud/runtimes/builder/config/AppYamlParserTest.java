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

import com.google.cloud.runtimes.builder.config.domain.BetaSettings;
import com.google.cloud.runtimes.builder.config.domain.EnvironmentVariablePrioritySetting;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests for {@link AppYamlParser}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(EnvironmentVariablePrioritySetting.class)
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
  public void testParseEnableAppEngineApisFalseWithoutEnvVar() throws IOException {
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "beta_settings:\n"
        + "  enable_app_engine_apis: false");
    assertFalse(result.getBetaSettings().isEnableAppEngineApis());
  }

  @Test
  public void testParseEnableAppEngineApisTrueWithEnvVar() throws IOException {
    PowerMockito.mockStatic(EnvironmentVariablePrioritySetting.class);
    PowerMockito.when(EnvironmentVariablePrioritySetting.getEnv("enable_app_engine_apis"))
        .thenReturn("true");
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "beta_settings:\n"
        + "  enable_app_engine_apis: false");
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
    PowerMockito.mockStatic(EnvironmentVariablePrioritySetting.class);
    PowerMockito.when(EnvironmentVariablePrioritySetting.getEnv("enable_app_engine_apis"))
        .thenReturn("true");
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE);
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
  public void testParseArtifactWithEnvVar() throws IOException {
    String artifact = "my/path/to/artifact";
    String otherArtifact = "my/path/to/other_artifact";
    PowerMockito.mockStatic(EnvironmentVariablePrioritySetting.class);
    PowerMockito.when(EnvironmentVariablePrioritySetting.getEnv("artifact"))
        .thenReturn(otherArtifact);
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  artifact: " + artifact);
    assertEquals(otherArtifact, result.getRuntimeConfig().getArtifact());
  }

  @Test
  public void testParseJdkWithEnvVar() throws IOException {
    String jdk = "jdk";
    String otherJdk = "other_jdk";
    PowerMockito.mockStatic(EnvironmentVariablePrioritySetting.class);
    PowerMockito.when(EnvironmentVariablePrioritySetting.getEnv("jdk"))
        .thenReturn(otherJdk);
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  jdk: " + jdk);
    assertEquals(otherJdk, result.getRuntimeConfig().getJdk());
  }

  @Test
  public void testParseBuildScriptWithEnvVar() throws IOException {
    String buildScript = "build_script";
    String otherBuildScript = "other_build_script";
    PowerMockito.mockStatic(EnvironmentVariablePrioritySetting.class);
    PowerMockito.when(EnvironmentVariablePrioritySetting.getEnv("build_script"))
        .thenReturn(otherBuildScript);
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE
        + "runtime_config:\n"
        + "  build_script: " + buildScript);
    assertEquals(otherBuildScript, result.getRuntimeConfig().getBuildScript());
  }

  @Test
  public void testParseArtifactWithoutYaml() throws IOException {
    String otherArtifact = "my/path/to/other_artifact";
    PowerMockito.mockStatic(EnvironmentVariablePrioritySetting.class);
    PowerMockito.when(EnvironmentVariablePrioritySetting.getEnv("artifact"))
        .thenReturn(otherArtifact);
    AppYaml result = parseFileWithContents(APP_YAML_PREAMBLE);
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
}
