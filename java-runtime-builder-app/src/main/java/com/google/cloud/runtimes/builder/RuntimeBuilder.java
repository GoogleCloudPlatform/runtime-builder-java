package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.build.BuildToolInvoker;
import com.google.cloud.runtimes.builder.build.BuildToolInvokerFactory;
import com.google.cloud.runtimes.builder.config.AppYamlParser;
import com.google.cloud.runtimes.builder.docker.DockerfileGenerator;
import com.google.cloud.runtimes.builder.exception.RuntimeBuilderException;
import com.google.cloud.runtimes.builder.injection.AppYamlPath;
import com.google.cloud.runtimes.builder.injection.WorkspacePath;
import com.google.cloud.runtimes.builder.workspace.Workspace;
import com.google.cloud.runtimes.builder.workspace.Workspace.WorkspaceBuilder;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Handles the main logic required to build a runtime.
 */
public class RuntimeBuilder {

  private static final String STAGING_DIR_NAME = "workspace_staging";

  private final Logger logger = LoggerFactory.getLogger(RuntimeBuilder.class);
  private final DockerfileGenerator dockerfileGenerator;
  private final BuildToolInvokerFactory buildToolInvokerFactory;
  private final Optional<Path> appYaml;
  private final AppYamlParser appYamlParser;
  private Path workspaceDir;

  /**
   * Constructs a new {@link RuntimeBuilder}.
   */
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

  /**
   * Runs through the main logic of building a runtime. Modifies the workspace directory so that it
   * only contains a deployable artifact and a Dockerfile.
   *
   * @throws IOException if low-level IO error was encountered
   * @throws RuntimeBuilderException if the given workspace is invalid and/or misconfigured
   */
  public void run() throws IOException, RuntimeBuilderException {
    // 0. Initialize and validate the workspace.
    Workspace workspace = new WorkspaceBuilder(appYamlParser, workspaceDir)
        .appYaml(appYaml.orElse(null))
        .build();

    // TODO there are two kinds of workspaces. 1) deployment from source, and 2) deploying a binary

    // if an artifact exists at root
      // construct "deployment"
    // else
      // construct source/workspace
      // deployment = workspace.build();

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
