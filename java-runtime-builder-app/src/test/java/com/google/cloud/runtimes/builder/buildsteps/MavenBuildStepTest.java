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
import com.google.cloud.runtimes.builder.util.StringLineAppender;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Unit tests for {@link MavenBuildStep}
 */
public class MavenBuildStepTest {

  private MavenBuildStep mavenBuildStep;
  private StringLineAppender dockerfileBuilder;
  private String mavenBuilderImage;

  @Mock private BuildContext buildContext;

  @Before
  public void before() throws IOException {
    dockerfileBuilder = new StringLineAppender();
    mavenBuilderImage = "gcr.io/foo/maven";

    MockitoAnnotations.initMocks(this);
    when(buildContext.getDockerfile()).thenReturn(dockerfileBuilder);

    mavenBuildStep = new MavenBuildStep(mavenBuilderImage);
  }

  // common assertions
  private void assertBuild() {
    assertTrue(dockerfileBuilder.toString().startsWith("FROM " + mavenBuilderImage + " as " + Constants.DOCKERFILE_BUILD_STAGE + "\n"));
    verify(buildContext, times(1)).setBuildArtifactLocation(
        eq(Optional.of(Paths.get("target"))));
  }

  @Test
  public void testRunWithWrapper() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("mvnw").setIsExecutable(true).build()
        .build();
    when(buildContext.getWorkspaceDir()).thenReturn(workspace);

    mavenBuildStep.run(buildContext);

    assertBuild();
    assertTrue(dockerfileBuilder.toString().contains("RUN ./mvnw "
        + "-B -DskipTests clean install\n"));
  }

  @Test
  public void testRunWithSystemGradle() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .build();
    when(buildContext.getWorkspaceDir()).thenReturn(workspace);

    mavenBuildStep.run(buildContext);

    assertBuild();
    assertTrue(dockerfileBuilder.toString().contains("RUN mvn -B -DskipTests clean install\n"));
  }

}
