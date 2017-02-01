package com.google.cloud.runtimes.builder.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import java.io.IOException;
import java.nio.file.Path;

public class AppYamlParser implements YamlParser<AppYaml> {

  private final ObjectMapper objectMapper;

  public AppYamlParser() {
    this.objectMapper = new ObjectMapper(new YAMLFactory());
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public AppYaml parse(Path yamlFilePath) throws IOException {
    return objectMapper.readValue(yamlFilePath.toFile(), AppYaml.class);
  }
}
