package com.google.cloud.runtimes.builder.workspace;

import static org.junit.Assert.assertEquals;

import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

/**
 * Unit tests for {@link Workspace}. See also {@link WorkspaceBuilderTest}.
 */
public class WorkspaceTest {

  private Path getTestDataDir() {
    return Paths.get("src/test/resources/testWorkspaces");
  }

  private Workspace createTestWorkspace(String workspacePath, ProjectType projectType) {
    return new Workspace(getTestDataDir().resolve(workspacePath), projectType, new RuntimeConfig(),
        false, null);
  }

  @Test
  public void testFindArtifact_simple()
      throws ArtifactNotFoundException, IOException, TooManyArtifactsException {
    Path artifact = createTestWorkspace("simple", ProjectType.NONE).findArtifact();
    assertEquals("foo.war", artifact.getFileName().toString());
  }

  @Test(expected = ArtifactNotFoundException.class)
  public void testFindArtifact_notFound()
      throws ArtifactNotFoundException, IOException, TooManyArtifactsException {
    createTestWorkspace("noArtifact", ProjectType.NONE).findArtifact();
  }

  @Test(expected = TooManyArtifactsException.class)
  public void testFindArtifact_tooMany()
      throws ArtifactNotFoundException, IOException, TooManyArtifactsException {
    createTestWorkspace("ambiguousArtifacts", ProjectType.NONE).findArtifact();
  }

  @Test
  public void testFindArtifact_mavenWorkspace()
      throws ArtifactNotFoundException, IOException, TooManyArtifactsException {
    Path artifact = createTestWorkspace("mavenWorkspace", ProjectType.MAVEN)
        .findArtifact();
    assertEquals("artifact.jar", artifact.getFileName().toString());
  }

  @Test
  public void testFindArtifact_gradleWorkspace()
      throws ArtifactNotFoundException, IOException, TooManyArtifactsException {
    Path artifact = createTestWorkspace("gradleWorkspace", ProjectType.GRADLE)
        .findArtifact();
    assertEquals("artifact.jar", artifact.getFileName().toString());
  }
}
