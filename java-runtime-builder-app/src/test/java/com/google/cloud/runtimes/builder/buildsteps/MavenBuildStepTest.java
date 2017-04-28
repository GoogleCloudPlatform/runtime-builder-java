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
 * Unit tests for {@link MavenBuildStep}
 */
public class MavenBuildStepTest {

  private String testMavenHomeDir;
  private MavenBuildStep mavenBuildStep;

  @Before
  public void setup() throws IOException {
    mavenBuildStep = spy(new MavenBuildStep());

    testMavenHomeDir = new TestWorkspaceBuilder()
        .file("bin/mvn").setIsExecutable(true).build()
        .build()
        .toString();

    when(mavenBuildStep.getMavenHome()).thenReturn(testMavenHomeDir);
  }

  @Test
  public void testBuildWithWrapper() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("mvnw").setIsExecutable(true).build()
        .build();

    List<String> buildCommand = mavenBuildStep.getBuildCommand(workspace);

    // assert that the first build command part is the mvnw wrapper executable
    assertEquals(workspace.resolve("mvnw").toString(), buildCommand.get(0));
  }

  @Test
  public void testBuildWithSystemMaven() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .build();

    List<String> buildCommand = mavenBuildStep.getBuildCommand(workspace);

    // assert that the system maven is called
    assertEquals(Paths.get(testMavenHomeDir, "bin", "mvn").toString(), buildCommand.get(0));
  }

  @Test(expected = IllegalStateException.class)
  public void testBuildWithNoMavenHomeEnvVariable() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .build();

    when(mavenBuildStep.getMavenHome()).thenReturn(null);

    mavenBuildStep.getBuildCommand(workspace);
  }

  @Test(expected = IllegalStateException.class)
  public void testBuildWithInvalidMavenHome() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .build();
    Path emptyMavenHome = new TestWorkspaceBuilder()
        .build();

    when(mavenBuildStep.getMavenHome()).thenReturn(emptyMavenHome.toString());

    mavenBuildStep.getBuildCommand(workspace);
  }
}
