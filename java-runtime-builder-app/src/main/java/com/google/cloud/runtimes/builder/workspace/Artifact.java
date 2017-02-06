package com.google.cloud.runtimes.builder.workspace;

import java.nio.file.Path;

/**
 * Encapsulates an artifact and possibly its dependencies.
 */
public abstract class Artifact {

  static Artifact get(Path path) {
    return null; // TODO
  }

  // TODO copy this artifact and its dependencies
  public abstract void copyTo(Path dest);

}
