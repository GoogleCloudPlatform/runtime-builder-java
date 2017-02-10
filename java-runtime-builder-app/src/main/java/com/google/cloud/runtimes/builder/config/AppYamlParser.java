package com.google.cloud.runtimes.builder.config;

import com.google.cloud.runtimes.builder.config.domain.AppYaml;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * YamlParser implementation that handles parsing of files in the {@link AppYaml} format.
 */
public class AppYamlParser implements YamlParser<AppYaml> {

  private final ObjectMapper objectMapper;

  /**
   * Constructs a new {@link AppYamlParser}.
   */
  public AppYamlParser() {
    this.objectMapper = new ObjectMapper(new YAMLFactory());
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public AppYaml parse(Path yamlFilePath) throws IOException {
    // TODO validation logic, i.e. should not have both build_tool and disable_build specified
    return objectMapper.readValue(yamlFilePath.toFile(), AppYaml.class);
  }
}
