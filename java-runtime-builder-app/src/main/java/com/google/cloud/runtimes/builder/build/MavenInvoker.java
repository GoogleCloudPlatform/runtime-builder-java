package com.google.cloud.runtimes.builder.build;

import com.google.cloud.runtimes.builder.exception.BuildToolInvokerException;
import com.google.cloud.runtimes.builder.workspace.Workspace;
import com.google.common.collect.ImmutableList;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

/**
 * Invokes a forked maven process. Expects that either the $M2_HOME env variable, or the system
 * property {@code maven.home} points to a valid maven installation.
 * <p>See also https://maven.apache.org/shared/maven-invoker/usage.html</p>
 */
public class MavenInvoker implements BuildToolInvoker {

  private final Logger logger = LoggerFactory.getLogger(MavenInvoker.class);

  @Override
  public void invoke(Workspace workspace) throws BuildToolInvokerException {
    logger.info("Invoking maven build");

    InvocationRequest request = new DefaultInvocationRequest();
    try {
      request.setPomFile(workspace.getBuildFile().toFile());
    } catch (FileNotFoundException e) {
      throw new BuildToolInvokerException("Maven build file not found", e);
    }
    request.setGoals(ImmutableList.of("-DskipTests=true", "clean", "install"));
    request.setBatchMode(true);
    Invoker invoker = new DefaultInvoker();
    try {
      InvocationResult result = invoker.execute(request);
      if (result.getExitCode() != 0) {
        throw new BuildToolInvokerException("Maven build failed.");
      }
    } catch (MavenInvocationException e) {
      throw new BuildToolInvokerException("An error was encountered invoking Maven", e);
    }
  }
}
