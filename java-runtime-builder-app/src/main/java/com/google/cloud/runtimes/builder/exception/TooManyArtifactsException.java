package com.google.cloud.runtimes.builder.exception;

import java.nio.file.Path;
import java.util.List;

public class TooManyArtifactsException extends RuntimeBuilderException {

  public TooManyArtifactsException(List<Path> artifacts) {
    super(buildMessage(artifacts));
  }

  private static String buildMessage(List<Path> artifacts) {
    StringBuilder sb = new StringBuilder();
    sb.append("Ambiguous deployable artifacts were found. Unable to proceed:\n");
    for (Path path : artifacts) {
      sb.append("\t" + path.toString() + "\n");
    }
    return sb.toString();
  }
}
