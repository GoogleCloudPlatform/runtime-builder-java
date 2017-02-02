package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.build.BuildToolInvoker;
import com.google.cloud.runtimes.builder.build.BuildToolInvokerFactory;
import com.google.cloud.runtimes.builder.config.AppYamlParser;
import com.google.cloud.runtimes.builder.docker.DockerfileGenerator;
import com.google.cloud.runtimes.builder.injection.AppYamlPath;
import com.google.cloud.runtimes.builder.injection.WorkspacePath;
import com.google.cloud.runtimes.builder.workspace.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.workspace.TooManyArtifactsException;
import com.google.cloud.runtimes.builder.workspace.Workspace;
import com.google.cloud.runtimes.builder.workspace.Workspace.WorkspaceBuilder;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class RuntimeBuilder {

  private final DockerfileGenerator dockerfileGenerator;
  private final BuildToolInvokerFactory buildToolInvokerFactory;
  private final Optional<Path> appYaml;
  private final AppYamlParser appYamlParser;
  private Path workspaceDir;

  @Inject
  public RuntimeBuilder(DockerfileGenerator dockerfileGenerator,
      BuildToolInvokerFactory buildToolInvokerFactory,
      AppYamlParser appYamlParser,
      @WorkspacePath Path workspaceDir, @AppYamlPath Optional<Path> appYaml) {
    this.dockerfileGenerator = dockerfileGenerator;
    this.buildToolInvokerFactory = buildToolInvokerFactory;
    this.appYamlParser = appYamlParser;
    this.workspaceDir = workspaceDir;
    this.appYaml = appYaml;
  }

  public void run() throws IOException {
    // 0. initialize and validate the workspace
    Workspace workspace = new WorkspaceBuilder(appYamlParser, workspaceDir)
          .appYaml(appYaml.orElse(null))
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
    } catch (ArtifactNotFoundException e) {
      // TODO
    }

    // 3. stage workspace
    Path STAGING_PATH = Paths.get("/workspace_staging");
    String workspaceDirName = workspaceDir.toString();

    // mkdir /workspace_staging
    Files.createDirectory(STAGING_PATH);

    // mv /workspace /workspace_staging
    Files.move(workspaceDir, STAGING_PATH);
    Path stagingDir = workspaceDir;

    // mkdir /workspace
    workspaceDir = Paths.get(workspaceDirName);
    Files.createDirectory(workspaceDir);

    // cp /workspace_staging/<path_to_deployable> workspace/<deployable_name>
//    Files.copy() TODO

    // 4. generate dockerfile
    String dockerfile = dockerfileGenerator.generateDockerfile(deployable);

    System.out.println("Generated dockerfile:\n");
    System.out.println(dockerfile);
  }

}
