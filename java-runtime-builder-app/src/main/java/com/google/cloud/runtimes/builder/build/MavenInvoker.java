package com.google.cloud.runtimes.builder.build;

import com.google.cloud.runtimes.builder.workspace.Workspace;
import com.google.common.collect.ImmutableList;
import java.io.File;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Invokes a forked maven process. Expects that either the $M2_HOME env variable, or the system
 * property {@code maven.home} points to a valid maven installation.
 *
 * See also https://maven.apache.org/shared/maven-invoker/usage.html
 */
public class MavenInvoker implements BuildToolInvoker {

  @Override
  public void invoke(Workspace workspace) {
    System.out.println("Invoking maven build");

    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(workspace.getBuildFile().toFile());
    request.setGoals(ImmutableList.of("clean", "install"));
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
