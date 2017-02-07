package com.google.cloud.runtimes.builder.build;

import com.google.cloud.runtimes.builder.workspace.Workspace;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileNotFoundException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Invokes a forked maven process. Expects that either the $M2_HOME env variable, or the system
 * property {@code maven.home} points to a valid maven installation.
 *
 * See also https://maven.apache.org/shared/maven-invoker/usage.html
 */
public class MavenInvoker implements BuildToolInvoker {

  private final Logger logger = LoggerFactory.getLogger(MavenInvoker.class);

  @Override
  public void invoke(Workspace workspace) {
    logger.info("Invoking maven build");

    InvocationRequest request = new DefaultInvocationRequest();
    try {
      request.setPomFile(workspace.getBuildFile().toFile());
    } catch (FileNotFoundException e) {
      // TODO rethrow
    }
    request.setGoals(ImmutableList.of("-DskipTests=true", "clean", "install"));
    request.setBatchMode(true);

    Invoker invoker = new DefaultInvoker();
    // TODO also get outputDir property from effective pom

    try {
      InvocationResult result = invoker.execute(request);
      if (result.getExitCode() != 0) {
        // TODO different exception type?
        throw new IllegalStateException("Maven build failed.");
      }
    } catch (MavenInvocationException e) {
      throw new RuntimeException("An error was encountered invoking Maven", e);
    }
  }
}
