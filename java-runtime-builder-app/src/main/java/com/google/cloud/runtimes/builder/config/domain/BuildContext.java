package com.google.cloud.runtimes.builder.config.domain;

import java.nio.file.Path;
import java.util.Optional;

public class BuildContext {

  private final RuntimeConfig runtimeConfig;
  private final Path workspaceDir;
  private final StringBuilder dockerfile;
  private final StringBuilder dockerignore;

  private Optional<Path> expectedArtifactDir;

  public BuildContext(RuntimeConfig runtimeConfig, Path workspaceDir) {
    this.runtimeConfig = runtimeConfig;
    this.workspaceDir = workspaceDir;
    this.dockerfile = new StringBuilder();
    this.dockerignore = new StringBuilder();

    expectedArtifactDir = Optional.empty();
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

  public Optional<Path> getExpectedArtifactDir() {
    return expectedArtifactDir;
  }

  public void setExpectedArtifactDir(Optional<Path> expectedArtifactDir) {
    this.expectedArtifactDir = expectedArtifactDir;
  }
}
