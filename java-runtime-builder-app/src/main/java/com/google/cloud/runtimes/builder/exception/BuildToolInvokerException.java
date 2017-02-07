package com.google.cloud.runtimes.builder.exception;

public class BuildToolInvokerException extends RuntimeBuilderException {

  public BuildToolInvokerException(String message) {
    super(message);
  }

  public BuildToolInvokerException(String message, Throwable cause) {
    super(message, cause);
  }
}
