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

package com.google.cloud.runtimes.builder;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepFactory;
import com.google.cloud.runtimes.builder.buildsteps.docker.StageDockerArtifactBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.gradle.GradleBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.maven.MavenBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.script.ScriptExecutionBuildStep;
import com.google.cloud.runtimes.builder.config.ConfigParser;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Unit tests for {@link BuildPipelineConfigurator}
 */
public class BuildPipelineConfiguratorTest {

  private static final String CONFIG_ENV_VARIABLE = "GCP_RUNTIME_BUILDER_CONFIG";

  @Mock private BuildStepFactory buildStepFactory;
  @Mock private MavenBuildStep mavenBuildStep;
  @Mock private GradleBuildStep gradleBuildStep;
  @Mock private StageDockerArtifactBuildStep stageDockerArtifactBuildStep;
  @Mock private ScriptExecutionBuildStep scriptExecutionBuildStep;
  @Mock private ConfigParser configParser;

  private RuntimeConfig runtimeConfig;
  private BuildPipelineConfigurator buildPipelineConfigurator;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);

    when(buildStepFactory.createMavenBuildStep()).thenReturn(mavenBuildStep);
    when(buildStepFactory.createGradleBuildStep()).thenReturn(gradleBuildStep);
    when(buildStepFactory.createStageDockerArtifactBuildStep())
        .thenReturn(stageDockerArtifactBuildStep);
    when(buildStepFactory.createScriptExecutionBuildStep(anyString()))
        .thenReturn(scriptExecutionBuildStep);

    runtimeConfig = new RuntimeConfig();
    when(configParser.parseFromEnvVar(eq(CONFIG_ENV_VARIABLE))).thenReturn(runtimeConfig);

    buildPipelineConfigurator = new BuildPipelineConfigurator(configParser, buildStepFactory);
  }

  @Test
  public void test_simpleWorkspace() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.war").build()
        .build();

    List<BuildStep> buildSteps = buildPipelineConfigurator.configurePipeline(workspace);
    assertEquals(1, buildSteps.size());
    assertEquals(stageDockerArtifactBuildStep, buildSteps.get(0));
  }

  @Test
  public void test_mavenWorkspace() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .build();

    List<BuildStep> buildSteps = buildPipelineConfigurator.configurePipeline(workspace);
    assertEquals(2, buildSteps.size());
    assertEquals(mavenBuildStep, buildSteps.get(0));
    assertEquals(stageDockerArtifactBuildStep, buildSteps.get(1));
  }

  @Test
  public void test_mavenAndGradleWorkspace() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .file("build.gradle").build()
        .build();

    List<BuildStep> buildSteps = buildPipelineConfigurator.configurePipeline(workspace);
    assertEquals(2, buildSteps.size());
    // maven takes precedence
    assertEquals(mavenBuildStep, buildSteps.get(0));
    assertEquals(stageDockerArtifactBuildStep, buildSteps.get(1));
  }

  @Test
  public void test_mavenWorkspace_customArtifact() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .build();

    runtimeConfig.setArtifact("my_output_dir/artifact.jar");

    List<BuildStep> buildSteps = buildPipelineConfigurator.configurePipeline(workspace);
    assertEquals(2, buildSteps.size());
    assertEquals(mavenBuildStep, buildSteps.get(0));
    assertEquals(stageDockerArtifactBuildStep, buildSteps.get(1));
    verify(stageDockerArtifactBuildStep, times(1))
        .setArtifactPathOverride(eq("my_output_dir/artifact.jar"));
  }

  @Test
  public void test_customBuildWorkspace() throws IOException {
    String buildScript = "gradle clean test buildThing";
    Path workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .build();

    runtimeConfig.setBuildScript(buildScript);

    List<BuildStep> buildSteps = buildPipelineConfigurator.configurePipeline(workspace);
    assertEquals(2, buildSteps.size());
    assertEquals(scriptExecutionBuildStep, buildSteps.get(0));
    assertEquals(stageDockerArtifactBuildStep, buildSteps.get(1));
    verify(buildStepFactory, times(1))
        .createScriptExecutionBuildStep(eq(buildScript));
  }

}
