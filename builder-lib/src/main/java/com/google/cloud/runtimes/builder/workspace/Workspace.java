package com.google.cloud.runtimes.builder.workspace;

import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.config.YamlParser;
import com.google.cloud.runtimes.builder.util.FileUtil;
import com.google.common.collect.ImmutableList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a directory containing a Java project.
 */
public class Workspace {

  private final static List<String> APP_YAML_LOCATIONS
      = ImmutableList.of("app.yaml", "src/main/appengine/app.yaml");

  private final static Map<String, ProjectType> PROJECT_TYPE_MAP;

  static {
    PROJECT_TYPE_MAP = new HashMap<>();
    PROJECT_TYPE_MAP.put("pom.xml", ProjectType.MAVEN);
    PROJECT_TYPE_MAP.put("build.gradle", ProjectType.GRADLE);
  }

  private final Path workspaceDir;
  private final Path buildFile;
  private final RuntimeConfig runtimeConfig;
  private final ProjectType projectType;
  private boolean requiresBuild;

  public Workspace(Path workspaceDir, ProjectType projectType, RuntimeConfig runtimeConfig, boolean requiresBuild, Optional<Path> buildFile) {
    this.workspaceDir = workspaceDir;
    this.runtimeConfig = runtimeConfig;
    this.projectType = projectType;
    this.requiresBuild = requiresBuild;
    this.buildFile = buildFile.orElse(null);

    // TODO valdiation, etc? or should this occur in the builder?
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

  public Path findArtifact() throws IOException, TooManyArtifactsException {
    // Check if the artifact location is specified in runtimeConfig.
    if (runtimeConfig.getArtifact() != null) {
      return Paths.get(runtimeConfig.getArtifact());
    } else {
      // Search for the artifact in the build output locations.
      Path buildOutputDir = workspaceDir.resolve(projectType.getOutputPath());
      List<Path> validArtifacts = FileUtil.findMatchingFilesInDir(buildOutputDir, file -> {
        String extension = FileUtil.getFileExtension(file.toPath());
        return extension.equals("war") || extension.equals("jar");
      });

      if (validArtifacts.size() < 1) {
        throw new FileNotFoundException();
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
        // Search for app.yaml within the workspace
        this.appYaml = APP_YAML_LOCATIONS.stream()
            .map(pathName -> workspaceDir.resolve(pathName))
            .filter(path -> Files.exists(path) && Files.isRegularFile(path))
            .findFirst()
            .orElseThrow(() -> new FileNotFoundException("app.yaml file not found"));
      }

      // Deserialize app.yaml
      RuntimeConfig runtimeConfig = appYamlParser.parse(appYaml).getRuntimeConfig();
      if (runtimeConfig == null) {
        // The user did not specify any options, use defaults.
        runtimeConfig = new RuntimeConfig();
      }

      // Identify the project type and locate the build file
      Optional<Path> buildFile = findBuildFile();
      ProjectType projectType = identifyProjectType(buildFile);
      boolean requiresBuild = buildFile.isPresent() && !runtimeConfig.getDisableRemoteBuild();

      return new Workspace(workspaceDir, projectType, runtimeConfig, requiresBuild, buildFile);
    }

    private Optional<Path> findBuildFile() throws IOException {
      // TODO make sure keys are checked in proper order, make sure runtime_config is respected
      Set<String> fileNames = PROJECT_TYPE_MAP.keySet();
      for (String fileName : fileNames) {
        List<Path> matches = FileUtil.findMatchingFilesInDir(workspaceDir,
          file -> file.getName().equals(fileName));
        if (!matches.isEmpty()) {
          return Optional.of(matches.get(0));
        }
      }
      return Optional.empty();
    }

    private ProjectType identifyProjectType(Optional<Path> buildFile) throws IOException {
      if (buildFile.isPresent()) {
        String fileName = buildFile.get().getFileName().toString();
        if (PROJECT_TYPE_MAP.containsKey(fileName)) {
          return PROJECT_TYPE_MAP.get(fileName);
        } else {
          throw new IllegalStateException(String.format(
              "Could not identify a project type for a build file named %s", fileName));
        }
      }
      return ProjectType.NONE;
    }
  }
}
