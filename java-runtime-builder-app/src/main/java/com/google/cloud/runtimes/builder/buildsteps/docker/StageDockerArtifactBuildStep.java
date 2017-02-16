package com.google.cloud.runtimes.builder.buildsteps.docker;

import com.google.cloud.runtimes.builder.buildsteps.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.MetadataConstants;
import com.google.cloud.runtimes.builder.exception.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.exception.BuildStepException;
import com.google.cloud.runtimes.builder.exception.TooManyArtifactsException;
import com.google.cloud.runtimes.builder.util.FileUtil;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StageDockerArtifactBuildStep extends BuildStep {

  private Optional<String> artifactPathOverride = Optional.empty();
  private final DockerfileGenerator dockerfileGenerator;

  @Inject
  StageDockerArtifactBuildStep(DockerfileGenerator dockerfileGenerator) {
    this.dockerfileGenerator = dockerfileGenerator;
  }

  @Override
  protected void doBuild(Path directory, Map<String, String> metadata) throws BuildStepException {
    try {
      Path artifact = getArtifact(directory, metadata);

      // TODO make staging dir
//    Generate dockerfile
      String dockerfile = dockerfileGenerator.generateDockerfile(artifact.getFileName());
//      Path dockerFileDest = originalWorkspaceDir.resolve("Dockerfile");
//
//    try (BufferedWriter writer
//        = Files.newBufferedWriter(dockerFileDest, StandardCharsets.US_ASCII)) {
//      writer.write(dockerfile);
//    }
    } catch (IOException | ArtifactNotFoundException | TooManyArtifactsException e) {
      throw new BuildStepException(e);
    }
  }

  /*
   * Returns the artifact to use for staging.
   */
  private Path getArtifact(Path directory, Map<String, String> metadata)
      throws ArtifactNotFoundException, IOException, TooManyArtifactsException {
    // if the artifact path has been overridden, use that value
    if (artifactPathOverride.isPresent()) {
      return directory.resolve(artifactPathOverride.get());
    }
    // if the artifact path is found in the metadata
    else if (metadata.containsKey(MetadataConstants.BUILD_ARTIFACT_PATH)) {
      // TODO if the metadata cant contain the exact path, still need to perform search in dir
      String buildArtifactPath = metadata.get(MetadataConstants.BUILD_ARTIFACT_PATH);
      Path buildOutputDir = directory.resolve(buildArtifactPath);
      return searchForArtifactInDir(buildOutputDir);
    }
    // otherwise, search for an artifact in the workspace root
    else {
      return searchForArtifactInDir(directory);
    }
  }

  /*
   * Searches for files that look like deployable artifacts in the given directory
   */
  private Path searchForArtifactInDir(Path directory) throws ArtifactNotFoundException,
      TooManyArtifactsException, IOException {
    Preconditions.checkArgument(Files.isDirectory(directory));

    List<Path> validArtifacts = new ArrayList<>();
    Files.list(directory)
        // filter out files that don't end in .war or .jar
        .filter((path) -> {
          String extension = FileUtil.getFileExtension(path);
          return extension.equals("war") || extension.equals("jar");
        })
        .forEach(validArtifacts::add);

    if (validArtifacts.size() < 1) {
      throw new ArtifactNotFoundException();
    } else if (validArtifacts.size() > 1) {
      throw new TooManyArtifactsException(validArtifacts);
    } else {
      return validArtifacts.get(0);
    }
  }


}
