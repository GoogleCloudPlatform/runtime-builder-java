package com.google.cloud.runtimes.builder.buildsteps;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.Constants;
import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Unit tests for {@link GradleBuildStep}
 */
public class GradleBuildStepTest {

  private GradleBuildStep gradleBuildStep;
  private StringBuilder dockerfileBuilder;
  private String gradleBuilderImage;

  @Mock private BuildContext buildContext;

  @Before
  public void before() throws IOException {
    dockerfileBuilder = new StringBuilder();
    gradleBuilderImage = "gcr.io/foo/gradle";

    MockitoAnnotations.initMocks(this);
    when(buildContext.getDockerfile()).thenReturn(dockerfileBuilder);

    gradleBuildStep = new GradleBuildStep(gradleBuilderImage);
  }

  // common assertions
  private void assertBuild() {
    assertTrue(dockerfileBuilder.toString().startsWith("FROM " + gradleBuilderImage + " as " + Constants.DOCKERFILE_BUILD_STAGE + "\n"));
    verify(buildContext, times(1)).setBuildArtifactLocation(
        eq(Optional.of(Paths.get("build/libs"))));
  }

  @Test
  public void testRunWithWrapper() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("gradlew").setIsExecutable(true).build()
        .build();
    when(buildContext.getWorkspaceDir()).thenReturn(workspace);

    gradleBuildStep.run(buildContext);

    assertBuild();
    assertTrue(dockerfileBuilder.toString().contains("RUN ./gradlew build\n"));
  }

  @Test
  public void testRunWithSystemGradle() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .build();
    when(buildContext.getWorkspaceDir()).thenReturn(workspace);

    gradleBuildStep.run(buildContext);

    assertBuild();
    assertTrue(dockerfileBuilder.toString().contains("RUN gradle build\n"));
  }

}
