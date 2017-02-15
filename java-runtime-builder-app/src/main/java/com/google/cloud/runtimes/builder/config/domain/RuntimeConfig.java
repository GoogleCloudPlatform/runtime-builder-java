package com.google.cloud.runtimes.builder.config.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuntimeConfig {

  private String buildScript;
  private String artifact;
  private String runtime;

  public String getArtifact() {
    return artifact;
  }

  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }


  public String getRuntime() {
    return this.runtime;
  }

  public void setRuntime(String runtime) {
    this.runtime = runtime;

  }

  public String getBuildScript() {
    return buildScript;
  }

  @JsonProperty("build_script")
  public void setBuildScript(String buildScript) {
    this.buildScript = buildScript;
  }
}
