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

import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.BetaSettings;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.injection.CommandLineOverrideSettings;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * YamlParser implementation that handles parsing of files in the {@link AppYaml} format.
 */
public class AppYamlParser implements YamlParser<AppYaml> {

  private final ObjectMapper objectMapper;

  private final Map<String, Object> overrideSettings;

  /**
   * Constructs a new {@link AppYamlParser}.
   * @param overrideSettings A map of the settings from the commandline.
   */
  @Inject
  public AppYamlParser(@CommandLineOverrideSettings Map<String, Object> overrideSettings) {
    Preconditions.checkNotNull(overrideSettings);
    this.objectMapper = new ObjectMapper(new YAMLFactory());
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    this.overrideSettings = overrideSettings;
  }

  /**
   * Constructs an instance without any overriding settings to the yaml file being parsed.
   */
  public AppYamlParser() {
    this(Collections.emptyMap());
  }

  @Override
  public AppYaml parse(Path yamlFilePath) throws IOException {
    AppYaml appYaml = objectMapper.readValue(yamlFilePath.toFile(), AppYaml.class);
    if (appYaml.getBetaSettings() == null) {
      appYaml.setBetaSettings(new BetaSettings());
    }
    if (appYaml.getRuntimeConfig() == null) {
      appYaml.setRuntimeConfig(new RuntimeConfig());
    }
    appYaml.applyOverrideSettings(this.overrideSettings);
    return appYaml;
  }
}
