package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.buildsteps.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.ScriptExecutionBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.docker.StageDockerArtifactBuildStep;
import com.google.cloud.runtimes.builder.config.YamlParser;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.BuildTool;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.throwingproviders.CheckedProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Fingerprints a directory and determines what set of build steps should be performed on it.
 */
public class BuildPipelineConfigurator {

  private static final List<String> APP_YAML_LOCATIONS
      = ImmutableList.of("app.yaml", "src/main/appengine/app.yaml");

  private final YamlParser<AppYaml> appYamlParser;
  private final StageDockerArtifactBuildStep stageDockerArtifactBuildStep;
  private final Path workspaceDir;

  @Inject
  BuildPipelineConfigurator(Path workspaceDir, YamlParser<AppYaml> appYamlParser,
      StageDockerArtifactBuildStep stageDockerArtifactBuildStep) {
    this.workspaceDir = workspaceDir;
    this.appYamlParser = appYamlParser;
    this.stageDockerArtifactBuildStep = stageDockerArtifactBuildStep;
  }

  /**
   * Examines a directory and returns a list of build steps that should be executed on it.
   *
   * @throws AppYamlNotFoundException if an app.yaml config file is not found
   * @throws IOException if a transient file system error is encountered
   */
  public List<BuildStep> getPipeline() throws AppYamlNotFoundException, IOException {
    // locate and deserialize configuration files
    AppYaml appYaml = appYamlParser.parse(findAppYaml(workspaceDir));
    RuntimeConfig runtimeConfig = appYaml.getRuntimeConfig() != null
        ? appYaml.getRuntimeConfig()
        : new RuntimeConfig();

    // assemble the list of build steps
    List<BuildStep> steps = new ArrayList<>();
    if (!Strings.isNullOrEmpty(runtimeConfig.getBuildScript())) {
      // the user has specified a command to build the project
      steps.add(new ScriptExecutionBuildStep(runtimeConfig.getBuildScript()));
    } else {
      // search for build files, keeping the first one we find
      Optional<Path> buildFile = findBuildFile(workspaceDir);
      if (buildFile.isPresent()) {
        BuildTool buildTool = BuildTool.getForBuildFile(buildFile.get());

        // TODO(alexsloan) support custom maven and gradle goals here
        List<String> buildToolGoals = new ArrayList<>();
        steps.add(buildTool.getBuildStep(buildToolGoals));
      }
    }

    steps.add(stageDockerArtifactBuildStep);
    return steps;
  }

  /*
   * Searches for app.yaml in a few expected paths within the workspace
   */
  private Path findAppYaml(Path workspaceDir) throws AppYamlNotFoundException {
    return APP_YAML_LOCATIONS.stream()
        .map(pathName -> workspaceDir.resolve(pathName))
        .filter(path -> Files.exists(path) && Files.isRegularFile(path))
        .findFirst()
        .orElseThrow(() -> new AppYamlNotFoundException("An app.yaml configuration file is "
            + "required, but was not found in the included sources."));
  }

  /*
   * Attempt to find a file in the workspace that looks like a build file.
   */
  private Optional<Path> findBuildFile(Path workspaceDir) throws IOException {
    return Files.list(workspaceDir)
        .filter((path) -> Files.isRegularFile(path))
        .filter(BuildTool::isABuildFile)
        // sort based on natural ordering of BuildTool for each path
        .sorted(Comparator.comparing(BuildTool::getForBuildFile))
        .findFirst();
  }
}
