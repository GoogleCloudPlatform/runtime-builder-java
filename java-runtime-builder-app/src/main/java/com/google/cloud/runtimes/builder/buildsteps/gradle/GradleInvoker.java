package com.google.cloud.runtimes.builder.buildsteps.gradle;

import com.google.cloud.runtimes.builder.exception.BuildToolInvokerException;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO class-level javadoc
public class GradleInvoker {

  private final Logger logger = LoggerFactory.getLogger(GradleInvoker.class);

  public void invoke(Path buildFile, List<String> args) throws BuildToolInvokerException {
    logger.info("Invoking gradle build");

    // TODO gradle build
    // see also https://docs.gradle.org/current/userguide/embedding.html
  }
}
