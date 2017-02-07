package com.google.cloud.runtimes.builder.workspace;

import com.google.cloud.runtimes.builder.config.YamlParser;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.BuildTool;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.cloud.runtimes.builder.exception.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.exception.TooManyArtifactsException;
import com.google.cloud.runtimes.builder.exception.WorkspaceConfigurationException;
import com.google.cloud.runtimes.builder.util.FileUtil;
import com.google.common.collect.ImmutableList;

import org.apache.commons.io.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Represents a directory containing a Java project.
 */
public class Workspace {

  private static final List<String> APP_YAML_LOCATIONS
      = ImmutableList.of("app.yaml", "src/main/appengine/app.yaml");

  private final RuntimeConfig runtimeConfig;
  private final Optional<BuildTool> buildTool;
  private final boolean requiresBuild;
  private Path workspaceDir;

  /**
   * Constructs a new {@link Workspace}.
   *
   * @param workspaceDir the directory containing the workspace
   * @param buildTool the buildTool that the workspace will be built with
   * @param runtimeConfig the user-provided configuration
   * @param requiresBuild true if a build must be performed in this workspace
   * @throws WorkspaceConfigurationException if the workspace requires a build, but is not buildable
   */
  Workspace(Path workspaceDir, Optional<BuildTool> buildTool, RuntimeConfig runtimeConfig,
      boolean requiresBuild) throws WorkspaceConfigurationException {
    this.workspaceDir = workspaceDir;
    this.runtimeConfig = runtimeConfig;
    this.buildTool = buildTool;
    this.requiresBuild = requiresBuild;

    if (requiresBuild() && !isBuildable()) {
      throw new WorkspaceConfigurationException(
          "The workspace requires a build, but is unable to be built.");
    }
  }

  /**
   * Returns the configured build tool.
   */
  public Optional<BuildTool> getBuildTool() {
    return this.buildTool;
  }

  /**
   * Finds and returns the build file that should be used to build the code in this workspace, based
   * on the {@link BuildTool} that is configured.
   *
   * @throws FileNotFoundException if a build file could not be found
   */
  public Path getBuildFile() throws FileNotFoundException {
    Path buildFilePath = workspaceDir.resolve(buildTool
        .orElseThrow(FileNotFoundException::new)
        .getBuildFileName());

    if (!Files.isRegularFile(buildFilePath)) {
      throw new FileNotFoundException(
          String.format("Expected build file to exist at %s, but none was found", buildFilePath));
    }
    return buildFilePath;
  }

  /**
   * Returns true if this workspace must be built before it can be deployed.
   */
  public boolean requiresBuild() {
    return requiresBuild;
  }

  /**
   * Returns true if this workspace is correctly configured for a build to be executed.
   */
  public boolean isBuildable() {
    boolean buildFileExists;
    try {
      getBuildFile();
      buildFileExists = true;
    } catch (FileNotFoundException e) {
      buildFileExists = false;
    }
    return getBuildTool().isPresent() && buildFileExists && !runtimeConfig.getDisableRemoteBuild();
  }

  /**
   * Returns a reference to the directory that this {@link Workspace} encapsulates.
   */
  public Path getWorkspaceDir() {
    return this.workspaceDir;
  }

  /**
   * Searches the workspace for a valid deployable artifact.
   *
   * @return the path to the discovered artifact
   * @throws IOException if a low-level IO exception was encountered
   * @throws TooManyArtifactsException if ambiguous artifacts were found
   * @throws ArtifactNotFoundException if no valid artifacts were found
   */
  public Path findArtifact()
      throws IOException, TooManyArtifactsException, ArtifactNotFoundException {
    // Check if the artifact location is specified in runtimeConfig.
    if (runtimeConfig.getArtifact() != null) {
      return workspaceDir.resolve(runtimeConfig.getArtifact());
    } else {
      // Scan the workspace for the artifact.
      Path pathToSearch;
      if (buildTool.isPresent()) {
        // TODO also check for overrides in pom.xml, build.gradle
        pathToSearch = workspaceDir.resolve(buildTool.get().getDefaultOutputPath());
      } else {
        pathToSearch = workspaceDir;
      }

      // Search for the artifact in the expected locations
      List<Path> validArtifacts = new ArrayList<>();
      if (Files.exists(pathToSearch) && Files.isDirectory(pathToSearch)) {
        Files.list(pathToSearch)
            // filter out files that don't end in .war or .jar
            .filter((path) -> {
              String extension = FileUtil.getFileExtension(path);
              return extension.equals("war") || extension.equals("jar");
            })
            .forEach(validArtifacts::add);
      }

      if (validArtifacts.size() < 1) {
        throw new ArtifactNotFoundException();
      } else if (validArtifacts.size() > 1) {
        throw new TooManyArtifactsException(validArtifacts);
      } else {
        return validArtifacts.get(0);
      }
    }
  }

  /**
   * Empties the workspace and moves all contents to the destination directory. The destination
   * directory becomes the new workspaceDir.
   * @param dest the destination for the move
   * @throws IOException if a low-level IO exception was encountered
   */
  public void moveContentsTo(Path dest) throws IOException {
    FileUtils.copyDirectory(workspaceDir.toFile(), dest.toFile());
    FileUtils.cleanDirectory(workspaceDir.toFile());
    workspaceDir = dest;
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

    /**
     * Builds the workspace.
     * @return the new Workspace instance
     * @throws AppYamlNotFoundException if no app.yaml configuration file was found
     * @throws IOException if a low-level IO exception was encountered
     * @throws WorkspaceConfigurationException if the workspace is not configured correctly
     */
    public Workspace build()
        throws AppYamlNotFoundException, IOException, WorkspaceConfigurationException {
      if (this.appYaml == null) {
        this.appYaml = findAppYaml();
      }

      // 1. Deserialize app.yaml
      AppYaml parsedAppYaml = appYamlParser.parse(appYaml);
      RuntimeConfig runtimeConfig = parsedAppYaml.getRuntimeConfig() != null
          ? parsedAppYaml.getRuntimeConfig()
          : new RuntimeConfig();

      // 2. Identify the build tool to use
      BuildTool buildTool;
      boolean requiresBuild = false;

      if (runtimeConfig.getBuildTool() != null) {
        // the user specified which build tool to use
        requiresBuild = true;
        buildTool = runtimeConfig.getBuildTool();
      } else {
        // search for valid build files, and take the first one we find
        Optional<Path> buildFile = findBuildFile();
        if (buildFile.isPresent()) {
          buildTool = BuildTool.getForBuildFile(buildFile.get());
        } else {
          // FYI this means the project cannot be built.
          // this will fail later if there's no artifact included in the sources.
          buildTool = null;
        }
      }
      return new Workspace(workspaceDir, Optional.ofNullable(buildTool), runtimeConfig,
          requiresBuild);
    }

    // Searches for app.yaml in a few expected paths within the workspace
    private Path findAppYaml() throws AppYamlNotFoundException {
      return APP_YAML_LOCATIONS.stream()
          .map(pathName -> workspaceDir.resolve(pathName))
          .filter(path -> Files.exists(path) && Files.isRegularFile(path))
          .findFirst()
          .orElseThrow(() -> new AppYamlNotFoundException("An app.yaml configuration file is "
              + "required, but was not found in the included sources."));
    }

    // Attempt to find a file in the workspace that looks like a build file.
    private Optional<Path> findBuildFile() throws IOException {
      return Files.list(workspaceDir)
          .filter((path) -> Files.isRegularFile(path))
          .filter((path) -> BuildTool.getForBuildFile(path) != null)
          // sort based on natural ordering of BuildTool for each path
          .sorted(Comparator.comparing(BuildTool::getForBuildFile))
          .findFirst();
    }
  }
}
