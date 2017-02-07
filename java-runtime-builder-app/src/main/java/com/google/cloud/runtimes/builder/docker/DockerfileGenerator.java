package com.google.cloud.runtimes.builder.docker;

import java.nio.file.Path;

public interface DockerfileGenerator {

  /**
   * Generate a Dockerfile for an artifact file at the given path.
   */
  String generateDockerfile(Path artifact);

}
