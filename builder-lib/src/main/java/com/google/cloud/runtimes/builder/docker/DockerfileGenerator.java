package com.google.cloud.runtimes.builder.docker;

import java.nio.file.Path;

public interface DockerfileGenerator {

  String generateDockerfile(Path artifact);

}
