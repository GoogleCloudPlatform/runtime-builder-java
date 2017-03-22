/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.runtimes.builder.buildsteps.docker;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepMetadataConstants;
import com.google.cloud.runtimes.builder.exception.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.exception.TooManyArtifactsException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Unit tests for {@link StageDockerArtifactBuildStep}.
 */
public class StageDockerArtifactBuildStepTest {

  @Mock
  private DockerfileGenerator dockerfileGenerator;
  private Map<String, String> metadata;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    when(dockerfileGenerator.generateDockerfile(any(Path.class))).thenReturn("");
    metadata = new HashMap<>();
  }

  @Test
  public void testDoBuild_withAmbiguousArtifacts() throws BuildStepException, IOException {
    String secondArtifactPath = "my_artifact.jar";
    Path workspace = new TestWorkspaceBuilder()
        .file("default.jar").build()
        .file(secondArtifactPath).build()
        .build();

    try {
      initBuildStep(null).doBuild(workspace, metadata);
    } catch (BuildStepException ex) {
      assertTrue(ex.getCause() instanceof TooManyArtifactsException);
      return;
    }
    fail();
  }

  @Test
  public void testDoBuild_withArtifactPathOverride() throws BuildStepException, IOException {
    String secondArtifactPath = "some_dir/my_artifact.jar";
    Path workspace = new TestWorkspaceBuilder()
        .file("default.jar").build()
        .file(secondArtifactPath).build()
        .build();

    initBuildStep(secondArtifactPath).doBuild(workspace, metadata);
    assertTrue(Files.exists(getDockerStagingDir(workspace).resolve("my_artifact.jar")));
  }

  @Test
  public void testDoBuild_withBuildArtifactMetadata() throws IOException, BuildStepException {
    String buildOutputDir = "target";
    Path workspace = new TestWorkspaceBuilder()
        .file("default.jar").build()
        .file(buildOutputDir + "/built_artifact.war").build()
        .build();

    metadata.put(BuildStepMetadataConstants.BUILD_ARTIFACT_PATH, buildOutputDir);

    initBuildStep(null).doBuild(workspace, metadata);
    assertTrue(Files.exists(getDockerStagingDir(workspace).resolve("built_artifact.war")));
  }

  @Test
  public void testDoBuild_withNoArtifact() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .build();

    try {
      initBuildStep(null).doBuild(workspace, metadata);
    } catch (BuildStepException ex) {
      assertTrue(ex.getCause() instanceof ArtifactNotFoundException);
      return;
    }
    fail();
  }

  private StageDockerArtifactBuildStep initBuildStep(String pathToArtifact) {
    StageDockerArtifactBuildStep buildStep = new StageDockerArtifactBuildStep(dockerfileGenerator);
    buildStep.setArtifactPathOverride(pathToArtifact);
    return buildStep;
  }

  private Path getDockerStagingDir(Path workspace) {
    return workspace.resolve(".docker_staging");
  }
}
