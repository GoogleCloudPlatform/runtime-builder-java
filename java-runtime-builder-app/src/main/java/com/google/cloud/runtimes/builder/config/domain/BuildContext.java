package com.google.cloud.runtimes.builder.config.domain;

import java.nio.file.Path;

public class BuildContext {

  private final RuntimeConfig runtimeConfig;
  private final Path workspaceDir;
  private final StringBuilder dockerfile;
  private final StringBuilder dockerignore;

  public BuildContext(RuntimeConfig runtimeConfig, Path workspaceDir) {
    this.runtimeConfig = runtimeConfig;
    this.workspaceDir = workspaceDir;
    this.dockerfile = new StringBuilder();
    this.dockerignore = new StringBuilder();
  }

  public RuntimeConfig getRuntimeConfig() {
    return runtimeConfig;
  }

  public Path getWorkspaceDir() {
    return workspaceDir;
  }

  public StringBuilder getDockerfile() {
    return dockerfile;
  }

  public StringBuilder getDockerignore() {
    return dockerignore;
  }
}
