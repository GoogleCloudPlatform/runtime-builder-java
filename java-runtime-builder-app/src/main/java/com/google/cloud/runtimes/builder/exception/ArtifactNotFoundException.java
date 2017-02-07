package com.google.cloud.runtimes.builder.exception;

public class ArtifactNotFoundException extends RuntimeBuilderException {

  public ArtifactNotFoundException() {
    super("No deployable artifacts were found. Unable to proceed.");
  }
}
