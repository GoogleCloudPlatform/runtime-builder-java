package com.google.cloud.runtimes.builder.exception;

public class RuntimeBuilderException extends Exception {

  public RuntimeBuilderException(String message) {
    super(message);
  }

  public RuntimeBuilderException(String message, Throwable cause) {
    super(message, cause);
  }

}
