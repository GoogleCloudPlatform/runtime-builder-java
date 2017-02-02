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
 * Invokes a maven build. See also https://maven.apache.org/shared/maven-invoker/usage.html
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
    // TODO read from env var / system property
    invoker.setMavenHome(new File("/usr/local/google/home/alexsloan/tools/apache-maven-3.3.9"));

    // TODO also get outputDir property from effective pom

    try {
      InvocationResult result = invoker.execute(request);
      if (result.getExitCode() != 0) {
        throw new IllegalStateException("Maven build failed.");
      }
    } catch (MavenInvocationException e) {
      throw new RuntimeException(e);
    }
  }
}
