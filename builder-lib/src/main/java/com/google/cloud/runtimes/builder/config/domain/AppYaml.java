package com.google.cloud.runtimes.builder.config.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppYaml {

  private RuntimeConfig runtimeConfig;

  @JsonProperty("runtime_config")
  public RuntimeConfig getRuntimeConfig() {
    return runtimeConfig;
  }

  public void setRuntimeConfig(RuntimeConfig runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
  }
}
