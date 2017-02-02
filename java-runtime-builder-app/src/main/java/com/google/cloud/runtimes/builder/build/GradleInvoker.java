package com.google.cloud.runtimes.builder.build;

import com.google.cloud.runtimes.builder.workspace.Workspace;

public class GradleInvoker implements BuildToolInvoker {

  @Override
  public void invoke(Workspace workspace) {
    System.out.println("Invoking gradle build");

    // TODO gradle build
    // see also https://docs.gradle.org/current/userguide/embedding.html
  }
}
