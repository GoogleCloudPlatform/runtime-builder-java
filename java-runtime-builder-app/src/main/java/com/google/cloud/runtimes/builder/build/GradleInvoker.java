package com.google.cloud.runtimes.builder.build;

import com.google.cloud.runtimes.builder.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GradleInvoker implements BuildToolInvoker {

  private final Logger logger = LoggerFactory.getLogger(GradleInvoker.class);

  @Override
  public void invoke(Workspace workspace) {
    logger.info("Invoking gradle build");

    // TODO gradle build
    // see also https://docs.gradle.org/current/userguide/embedding.html
  }
}
