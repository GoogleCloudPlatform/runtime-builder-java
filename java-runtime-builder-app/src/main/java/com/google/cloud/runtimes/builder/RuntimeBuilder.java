package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.build.BuildToolInvoker;
import com.google.cloud.runtimes.builder.build.BuildToolInvokerFactory;
import com.google.cloud.runtimes.builder.config.AppYamlParser;
import com.google.cloud.runtimes.builder.docker.DockerfileGenerator;
import com.google.cloud.runtimes.builder.injection.AppYamlPath;
import com.google.cloud.runtimes.builder.injection.WorkspacePath;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.cloud.runtimes.builder.exception.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.exception.WorkspaceConfigurationException;
import com.google.cloud.runtimes.builder.exception.TooManyArtifactsException;
import com.google.cloud.runtimes.builder.workspace.Workspace;
import com.google.cloud.runtimes.builder.workspace.Workspace.WorkspaceBuilder;
import com.google.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeBuilder {

  private static final String STAGING_DIR_NAME = "workspace_staging";

  private final Logger logger = LoggerFactory.getLogger(RuntimeBuilder.class);
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

  public void run()
      throws IOException, AppYamlNotFoundException, WorkspaceConfigurationException, TooManyArtifactsException, ArtifactNotFoundException {
    // 0. Initialize and validate the workspace.
    Workspace workspace = new WorkspaceBuilder(appYamlParser, workspaceDir)
        .appYaml(appYaml.orElse(null))
        .build();

    // 1. Build the project if necessary
    if (workspace.isBuildable()) {
      logger.info("Initiating building your source...");
      BuildToolInvoker buildTool = buildToolInvokerFactory.get(workspace.getBuildTool().get());
      buildTool.invoke(workspace);
    }

    // 2. Clear the source files from the workspace
    Path originalWorkspaceDir = workspace.getWorkspaceDir();
    Path stagingPath = workspace.getWorkspaceDir().resolveSibling(Paths.get(STAGING_DIR_NAME));
    workspace.moveContentsTo(stagingPath);

    // 3. Try to find an artifact to deploy
    Path deployable = workspace.findArtifact();

    // 4. Put the artifact at the root of the original workspace
    Files.copy(deployable, (deployable = originalWorkspaceDir.resolve(deployable.getFileName())));
    logger.info("Preparing to deploy artifact " + deployable.toString());

    // 5. Generate dockerfile
    String dockerfile = dockerfileGenerator.generateDockerfile(deployable.getFileName());
    Path dockerFileDest = originalWorkspaceDir.resolve("Dockerfile");

    try (BufferedWriter writer
        = Files.newBufferedWriter(dockerFileDest, StandardCharsets.US_ASCII)) {
      writer.write(dockerfile);
    }

    logger.info("Generated dockerfile:");
    logger.info(dockerfile);
  }

}
