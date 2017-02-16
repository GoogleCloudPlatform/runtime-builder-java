package com.google.cloud.runtimes.builder.buildsteps.maven;

import com.google.cloud.runtimes.builder.exception.BuildToolInvokerException;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.List;
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
 * <p>See also https://maven.apache.org/shared/maven-invoker/usage.html</p>
 */
public class MavenInvoker {

  private final Logger logger = LoggerFactory.getLogger(MavenInvoker.class);
  private final List<String> DEFAULT_GOALS
      = ImmutableList.of("-B", "-DskipTests=true", "clean", "package");

  public void invoke(Path pomFile, List<String> goals) throws BuildToolInvokerException {
    logger.info("Invoking maven");

    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(pomFile.toFile());
    request.setGoals(goals);
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

  public void invoke(Path pomFile) throws BuildToolInvokerException {
    invoke(pomFile, DEFAULT_GOALS);
  }
}
