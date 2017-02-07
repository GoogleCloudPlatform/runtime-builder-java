package com.google.cloud.runtimes.builder.config.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuntimeConfig {

  private boolean disableRemoteBuild = false;
  private BuildTool buildTool;
  private String artifact;
  private String server;
  private String jdk;

  @JsonProperty("disable_remote_build")
  public boolean getDisableRemoteBuild() {
    return disableRemoteBuild;
  }

  public void setDisableRemoteBuild(boolean disableRemoteBuild) {
    this.disableRemoteBuild = disableRemoteBuild;
  }

  @JsonProperty("build_tool")
  public BuildTool getBuildTool() {
    return buildTool;
  }

  public void setBuildTool(BuildTool buildTool) {
    this.buildTool = buildTool;
  }

  public String getArtifact() {
    return artifact;
  }

  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public String getJdk() {
    return jdk;
  }

  public void setJdk(String jdk) {
    this.jdk = jdk;
  }

}
