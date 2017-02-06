package com.google.cloud.runtimes.builder.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Unit tests for {@link Workspace}. See also {@link WorkspaceBuilderTest}.
 */
public class WorkspaceTest {

  private Path getTestDataDir(String resourcePath) throws IOException {
    String resourceName = "/testWorkspaces" + "/" + resourcePath;
    String name = getClass().getResource(resourceName).getFile();
    // make a copy to ensure test independence
    Path destDir = Files.createTempDirectory(null);
    FileUtils.copyDirectory(Paths.get(name).toFile(), destDir.toFile());
    return destDir;
  }

  private Workspace createTestWorkspace(String workspacePath, ProjectType projectType)
      throws IOException {
    return new Workspace(getTestDataDir(workspacePath), projectType, new RuntimeConfig(),
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

  @Test
  public void testMoveContentsTo()
      throws IOException, TooManyArtifactsException, ArtifactNotFoundException {
    Workspace workspace = createTestWorkspace("mavenWorkspace", ProjectType.MAVEN);

    // make a tmp copy for performing assertions on content
    Path originalWorkspaceCopy = Files.createTempDirectory(null);
    FileUtils.copyDirectory(workspace.getWorkspaceDir().toFile(), originalWorkspaceCopy.toFile());

    String originalDirName = workspace.getWorkspaceDir().toString();

    Path dest = Files.createTempDirectory(null);
    workspace.moveContentsTo(dest);

    // assert that the workspace has updated its reference to the new dir
    assertEquals(dest, workspace.getWorkspaceDir());
    // assert that the original dir is now empty
    assertEquals(0, Files.list(Paths.get(originalDirName)).count());

    // make sure the new directory structure is the same as it was before
    Files.walk(workspace.getWorkspaceDir()).forEach((path) -> {
      try {
        if (!Files.isDirectory(path)) {
          int workspaceRootSize = workspace.getWorkspaceDir().getNameCount();

          Path subPathInWorkspace = path.subpath(workspaceRootSize, path.getNameCount());
          Path originalWorkspacePath = originalWorkspaceCopy.resolve(subPathInWorkspace);
          assertTrue(FileUtils.contentEquals(path.toFile(), originalWorkspacePath.toFile()));
        }

      } catch (IOException e) {
        fail();
      }
    });

    // assert that we can still find the artifact in the new directory
    assertEquals("artifact.jar", workspace.findArtifact().getFileName().toString());
  }
}
