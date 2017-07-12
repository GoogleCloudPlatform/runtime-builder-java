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

import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.buildsteps.GradleBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.JettyOptionsBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.MavenBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.PrebuiltRuntimeImageBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.ScriptExecutionBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.SourceBuildRuntimeImageBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStep;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepFactory;
import com.google.cloud.runtimes.builder.config.AppYamlFinder;
import com.google.cloud.runtimes.builder.config.AppYamlParser;
import com.google.cloud.runtimes.builder.config.YamlParser;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.common.base.Objects;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Unit tests for {@link BuildPipelineConfigurator}
 */
public class BuildPipelineConfiguratorTest {

  @Mock private BuildStepFactory buildStepFactory;
  @Mock private MavenBuildStep mavenBuildStep;
  @Mock private GradleBuildStep gradleBuildStep;
  @Mock private ScriptExecutionBuildStep scriptExecutionBuildStep;
  @Mock private PrebuiltRuntimeImageBuildStep prebuiltRuntimeImageBuildStep;
  @Mock private SourceBuildRuntimeImageBuildStep sourceBuildRuntimeImageBuildStep;
  @Mock private JettyOptionsBuildStep jettyOptionsBuildStep;

  // use the actual yaml parser and yaml finders instead of mocks
  private YamlParser<AppYaml> appYamlYamlParser = new AppYamlParser();
  private AppYamlFinder appYamlFinder = new AppYamlFinder(Optional.empty());
  private BuildPipelineConfigurator buildPipelineConfigurator;

  @Before
  public void setup() throws BuildStepException {
    MockitoAnnotations.initMocks(this);

    when(buildStepFactory.createMavenBuildStep()).thenReturn(mavenBuildStep);
    when(buildStepFactory.createGradleBuildStep()).thenReturn(gradleBuildStep);
    when(buildStepFactory.createScriptExecutionBuildStep(anyString()))
        .thenReturn(scriptExecutionBuildStep);
    when(buildStepFactory.createPrebuiltRuntimeImageBuildStep())
        .thenReturn(prebuiltRuntimeImageBuildStep);
    when(buildStepFactory.createSourceBuildRuntimeImageStep())
        .thenReturn(sourceBuildRuntimeImageBuildStep);
    when(buildStepFactory.createJettyOptionsBuildStep()).thenReturn(jettyOptionsBuildStep);

    buildPipelineConfigurator
        = new BuildPipelineConfigurator(appYamlYamlParser, appYamlFinder, buildStepFactory);
  }

  private void assertBuildStepsCalledWithRuntimeConfig(RuntimeConfig expected,
      BuildStep... buildSteps) throws BuildStepException {
    for (BuildStep buildStep : buildSteps) {
      ArgumentCaptor<BuildContext> captor = ArgumentCaptor.forClass(BuildContext.class);
      verify(buildStep, times(1)).run(captor.capture());
      assertRuntimeConfigEquals(expected, captor.getValue().getRuntimeConfig());
    }
  }

  private void assertRuntimeConfigEquals(RuntimeConfig expected, RuntimeConfig actual) {
    assertTrue(Objects.equal(expected.getJdk(), actual.getJdk())
        && Objects.equal(expected.getArtifact(), actual.getArtifact())
        && Objects.equal(expected.getServer(), actual.getServer())
        && Objects.equal(expected.getBuildScript(), actual.getBuildScript())
        && Objects.equal(expected.getJettyQuickstart(), actual.getJettyQuickstart()));
  }

  @Test
  public void testPrebuiltArtifact() throws BuildStepException, IOException,
      AppYamlNotFoundException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.war").build()
        .build();

    buildPipelineConfigurator.generateDockerResources(workspace);

    verify(buildStepFactory, times(1)).createPrebuiltRuntimeImageBuildStep();
    verify(buildStepFactory, times(1)).createJettyOptionsBuildStep();
    verifyNoMoreInteractions(buildStepFactory);

    assertBuildStepsCalledWithRuntimeConfig(new RuntimeConfig(), prebuiltRuntimeImageBuildStep,
        jettyOptionsBuildStep);
  }

  @Test
  public void testMavenSourceBuild() throws BuildStepException, IOException,
      AppYamlNotFoundException {
    Path workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .build();

    buildPipelineConfigurator.generateDockerResources(workspace);

    verify(buildStepFactory, times(1)).createMavenBuildStep();
    verify(buildStepFactory, times(1)).createSourceBuildRuntimeImageStep();
    verify(buildStepFactory, times(1)).createJettyOptionsBuildStep();
    verifyNoMoreInteractions(buildStepFactory);

    assertBuildStepsCalledWithRuntimeConfig(new RuntimeConfig(), mavenBuildStep,
        sourceBuildRuntimeImageBuildStep, jettyOptionsBuildStep);
  }

  @Test
  public void testGradleSourceBuild() throws BuildStepException, IOException,
      AppYamlNotFoundException {
    Path workspace = new TestWorkspaceBuilder()
        .file("build.gradle").build()
        .build();

    buildPipelineConfigurator.generateDockerResources(workspace);

    verify(buildStepFactory, times(1)).createGradleBuildStep();
    verify(buildStepFactory, times(1)).createSourceBuildRuntimeImageStep();
    verify(buildStepFactory, times(1)).createJettyOptionsBuildStep();
    verifyNoMoreInteractions(buildStepFactory);

    assertBuildStepsCalledWithRuntimeConfig(new RuntimeConfig(), gradleBuildStep,
        sourceBuildRuntimeImageBuildStep, jettyOptionsBuildStep);
  }

  @Test
  public void testMavenAndGradleSourceBuild() throws BuildStepException, IOException,
      AppYamlNotFoundException {
    Path workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .file("build.gradle").build()
        .build();

    buildPipelineConfigurator.generateDockerResources(workspace);

    verify(buildStepFactory, times(1)).createMavenBuildStep();
    verify(buildStepFactory, times(1)).createSourceBuildRuntimeImageStep();
    verify(buildStepFactory, times(1)).createJettyOptionsBuildStep();
    verifyNoMoreInteractions(buildStepFactory);

    assertBuildStepsCalledWithRuntimeConfig(new RuntimeConfig(), mavenBuildStep,
        sourceBuildRuntimeImageBuildStep, jettyOptionsBuildStep);
  }

  @Test
  public void testMavenBuildWithCustomScript() throws BuildStepException, IOException,
      AppYamlNotFoundException {
    String customScript = "custom mvn goals";
    Path workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .file("app.yaml").withContents(
            "runtime_config:\n"
            + "  build_script: " + customScript).build()
        .build();

    buildPipelineConfigurator.generateDockerResources(workspace);

    verify(buildStepFactory, times(1)).createScriptExecutionBuildStep(eq(customScript));
    verify(buildStepFactory, times(1)).createSourceBuildRuntimeImageStep();
    verify(buildStepFactory, times(1)).createJettyOptionsBuildStep();
    verifyNoMoreInteractions(buildStepFactory);

    RuntimeConfig expectedConfig = new RuntimeConfig();
    expectedConfig.setBuildScript(customScript);
    assertBuildStepsCalledWithRuntimeConfig(expectedConfig, scriptExecutionBuildStep,
        sourceBuildRuntimeImageBuildStep, jettyOptionsBuildStep);
  }

  @Test
  public void testPrebuiltArtifactAndMavenBuild() throws BuildStepException, IOException,
      AppYamlNotFoundException {
    Path workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .file("foo.war").build()
        .build();

    buildPipelineConfigurator.generateDockerResources(workspace);

    verify(buildStepFactory, times(1)).createMavenBuildStep();
    verify(buildStepFactory, times(1)).createSourceBuildRuntimeImageStep();
    verify(buildStepFactory, times(1)).createJettyOptionsBuildStep();
    verifyNoMoreInteractions(buildStepFactory);

    assertBuildStepsCalledWithRuntimeConfig(new RuntimeConfig(), mavenBuildStep,
        sourceBuildRuntimeImageBuildStep, jettyOptionsBuildStep);
  }

}
