package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.build.BuildToolInvoker;
import com.google.cloud.runtimes.builder.build.BuildToolInvokerFactory;
import com.google.cloud.runtimes.builder.config.AppYamlParser;
import com.google.cloud.runtimes.builder.docker.DockerfileGenerator;
import com.google.cloud.runtimes.builder.injection.AppYamlPath;
import com.google.cloud.runtimes.builder.injection.WorkspacePath;
import com.google.cloud.runtimes.builder.workspace.TooManyArtifactsException;
import com.google.cloud.runtimes.builder.workspace.Workspace;
import com.google.cloud.runtimes.builder.workspace.Workspace.WorkspaceBuilder;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

public class RuntimeBuilder {

  private final DockerfileGenerator dockerfileGenerator;
  private final BuildToolInvokerFactory buildToolInvokerFactory;
  private final Path appYaml;
  private final Path workspaceDir;
  private final AppYamlParser appYamlParser;

  @Inject
  public RuntimeBuilder(DockerfileGenerator dockerfileGenerator,
      BuildToolInvokerFactory buildToolInvokerFactory,
      AppYamlParser appYamlParser,
      @WorkspacePath Path workspaceDir, @AppYamlPath Path appYaml) {
    this.dockerfileGenerator = dockerfileGenerator;
    this.buildToolInvokerFactory = buildToolInvokerFactory;
    this.appYamlParser = appYamlParser;
    this.workspaceDir = workspaceDir;
    this.appYaml = appYaml;
  }

  public void run() throws IOException {
    // 0. initialize and validate the workspace
    Workspace workspace = new WorkspaceBuilder(appYamlParser, workspaceDir)
          .appYaml(appYaml)
          .build();

    // 1. build the project if necessary
    if (workspace.requiresBuild()) {
      BuildToolInvoker buildTool = buildToolInvokerFactory.get(workspace.getProjectType());
      buildTool.invoke(workspace);
      workspace.setRequiresBuild(false);
    }

    // 2. try to find an artifact to deploy
    Path deployable = null;
    try {
      deployable = workspace.findArtifact();
    } catch (TooManyArtifactsException e) {
      // TODO
      e.printStackTrace();
    }

    // 3. generate dockerfile
    String dockerfile = dockerfileGenerator.generateDockerfile(deployable);

    System.out.println("Generated dockerfile:\n");
    System.out.println(dockerfile);
  }

}
