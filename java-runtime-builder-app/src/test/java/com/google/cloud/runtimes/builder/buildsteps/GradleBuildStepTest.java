package com.google.cloud.runtimes.builder.buildsteps;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link GradleBuildStep}
 */
public class GradleBuildStepTest {

  private String testGradleHome;
  private GradleBuildStep gradleBuildStep;

  @Before
  public void before() throws IOException {
    gradleBuildStep = spy(new GradleBuildStep());

    testGradleHome = new TestWorkspaceBuilder()
        .file("bin/gradle").setIsExecutable(true).build()
        .build()
        .toString();

    when(gradleBuildStep.getGradleHome()).thenReturn(testGradleHome);
  }

  @Test
  public void testBuildWithWrapper() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("gradlew").setIsExecutable(true).build()
        .build();

    List<String> buildCommand = gradleBuildStep.getBuildCommand(workspace);

    // assert that the first build command part is the gradlew wrapper executable
    assertEquals(workspace.resolve("gradlew").toString(), buildCommand.get(0));
  }

  @Test
  public void testBuildWithSystemGradle() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .build();

    List<String> buildCommand = gradleBuildStep.getBuildCommand(workspace);

    // assert that the system gradle is called
    assertEquals(Paths.get(testGradleHome, "bin", "gradle").toString(), buildCommand.get(0));
  }

  @Test(expected = IllegalStateException.class)
  public void testBuildWithNoGradleHomeEnvVariable() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .build();

    when(gradleBuildStep.getGradleHome()).thenReturn(null);

    gradleBuildStep.getBuildCommand(workspace);
  }

  @Test(expected = IllegalStateException.class)
  public void testBuildWithInvalidGradleHome() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .build();
    Path emptyGradleHome = new TestWorkspaceBuilder()
        .build();

    when(gradleBuildStep.getGradleHome()).thenReturn(emptyGradleHome.toString());

    gradleBuildStep.getBuildCommand(workspace);
  }
}
