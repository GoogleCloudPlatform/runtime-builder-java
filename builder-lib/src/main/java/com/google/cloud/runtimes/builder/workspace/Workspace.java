package com.google.cloud.runtimes.builder.workspace;

import com.google.cloud.runtimes.builder.config.YamlParser;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.util.FileUtil;
import com.google.common.collect.ImmutableList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a directory containing a Java project.
 */
public class Workspace {

  private final static List<String> APP_YAML_LOCATIONS
      = ImmutableList.of("app.yaml", "src/main/appengine/app.yaml");

  private final Path workspaceDir;
  private final Path buildFile;
  private final RuntimeConfig runtimeConfig;
  private final ProjectType projectType;
  private boolean requiresBuild;

  public Workspace(Path workspaceDir, ProjectType projectType, RuntimeConfig runtimeConfig,
      boolean requiresBuild, Path buildFile) {
    this.workspaceDir = workspaceDir;
    this.runtimeConfig = runtimeConfig;
    this.projectType = projectType;
    this.requiresBuild = requiresBuild;
    this.buildFile = buildFile;
  }

  public ProjectType getProjectType() {
    return this.projectType;
  }

  public Path getBuildFile() {
    return this.buildFile;
  }

  public boolean requiresBuild() {
    return this.requiresBuild;
  }

  public void setRequiresBuild(boolean requiresBuild) {
    this.requiresBuild = requiresBuild;
  }

  public Path findArtifact()
      throws IOException, TooManyArtifactsException, ArtifactNotFoundException {
    // Check if the artifact location is specified in runtimeConfig.
    if (runtimeConfig.getArtifact() != null) {
      return workspaceDir.resolve(runtimeConfig.getArtifact());
    } else {
      // Search for the artifact in the build output locations.
      // TODO also check for overrides in pom.xml, build.gradle
      Path buildOutputDir = workspaceDir.resolve(projectType.getDefaultOutputPath());

      List<Path> validArtifacts = Files.list(buildOutputDir)
      // filter out files that don't end in .war or .jar
      .filter((path) -> {
        String extension = FileUtil.getFileExtension(path);
        return extension.equals("war") || extension.equals("jar");
      })
      .collect(Collectors.toList());

      if (validArtifacts.size() < 1) {
        throw new ArtifactNotFoundException();
      } else if (validArtifacts.size() > 1) {
        throw new TooManyArtifactsException();
      } else {
        return validArtifacts.get(0);
      }
    }
  }

  /**
   * Builder class to encapsulate logic for constructing instances of {@link Workspace}.
   */
  public static class WorkspaceBuilder {

    private final Path workspaceDir;
    private final YamlParser<AppYaml> appYamlParser;
    private Path appYaml;

    public WorkspaceBuilder(YamlParser<AppYaml> appYamlParser, Path workspaceDir) {
      this.appYamlParser = appYamlParser;
      this.workspaceDir = workspaceDir;
    }

    public WorkspaceBuilder appYaml(Path appYaml) {
      this.appYaml = appYaml;
      return this;
    }

    public Workspace build() throws IOException {
      if (this.appYaml == null) {
        this.appYaml = findAppYaml();
      }

      // 1. Deserialize app.yaml
      AppYaml parsedAppYaml = appYamlParser.parse(appYaml);
      RuntimeConfig runtimeConfig = parsedAppYaml.getRuntimeConfig() != null
          ? parsedAppYaml.getRuntimeConfig()
          : new RuntimeConfig();

      // 2. Identify the project type and locate the build file
      ProjectType projectType;
      Optional<Path> buildFile;
      if (runtimeConfig.getBuildTool() != null) {
        // the user specified which build tool to use
        projectType = ProjectType.getForBuildToolName(runtimeConfig.getBuildTool());
        buildFile = Optional.of(workspaceDir.resolve(projectType.getBuildFileName()));
      } else {
        // search for an appropriate build tool
        buildFile = findBuildFile(); // TODO we should really be able to find both the project type and build file in one go
        projectType = identifyProjectType(buildFile, runtimeConfig);
      }

      boolean requiresBuild = buildFile.isPresent() && !runtimeConfig.getDisableRemoteBuild();

      return new Workspace(workspaceDir, projectType, runtimeConfig, requiresBuild,
          buildFile.orElse(null));
    }

    // Searches for app.yaml within the workspace
    private Path findAppYaml() throws FileNotFoundException {
      return APP_YAML_LOCATIONS.stream()
          .map(pathName -> workspaceDir.resolve(pathName))
          .filter(path -> Files.exists(path) && Files.isRegularFile(path))
          .findFirst()
          .orElseThrow(() -> new FileNotFoundException("app.yaml file not found"));
    }

    private Optional<Path> findBuildFile() throws IOException {
      // Find a file in the workspace that looks like a recognized build file.
      return Files.list(workspaceDir)
          .filter((path) -> Files.isRegularFile(path))
          .filter((path) -> ProjectType.getForBuildFileName(path.getFileName().toString()) != null)
          .findFirst();
    }

    private ProjectType identifyProjectType(Optional<Path> buildFile, RuntimeConfig runtimeConfig)
        throws IOException {
      if (runtimeConfig.getBuildTool() != null) {
        // if the build tool has
        return ProjectType.getForBuildToolName(runtimeConfig.getBuildTool());
      } else if (buildFile.isPresent()) {
        return ProjectType.getForBuildFileName(buildFile.get().getFileName().toString());
      } else {
        return ProjectType.NONE;
      }
    }
  }
}
