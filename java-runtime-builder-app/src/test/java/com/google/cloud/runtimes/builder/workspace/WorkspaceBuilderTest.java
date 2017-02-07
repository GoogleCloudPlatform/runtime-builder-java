package com.google.cloud.runtimes.builder.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.cloud.runtimes.builder.config.AppYamlParser;
import com.google.cloud.runtimes.builder.config.domain.BuildTool;
import com.google.cloud.runtimes.builder.workspace.Workspace.WorkspaceBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Test;

/**
 * Unit tests for {@link com.google.cloud.runtimes.builder.workspace.Workspace.WorkspaceBuilder}.
 */
public class WorkspaceBuilderTest {

  private AppYamlParser appYamlParser = new AppYamlParser();

  @Test
  public void testBuild_maven() throws Exception {
    Workspace workspace = initBuilder("mavenWorkspace").build();

    assertEquals(BuildTool.MAVEN, workspace.getBuildTool().get());
    assertFalse(workspace.requiresBuild());
    assertTrue(workspace.isBuildable());
    assertTrue(Files.isSameFile(getTestDir("mavenWorkspace").resolve("pom.xml"),
        workspace.getBuildFile()));
    assertTrue(Files.isSameFile(getTestDir("mavenWorkspace").resolve("target/artifact.jar"),
        workspace.findArtifact()));
  }

  @Test
  public void testBuild_maven_withRuntimeConfig() throws Exception {
    Workspace workspace = initBuilder("mavenWorkspace_customized")
        .appYaml(getTestDir("mavenWorkspace_customized").resolve("appengine/app.yaml"))
        .build();

    assertEquals(BuildTool.MAVEN, workspace.getBuildTool().get());
    assertFalse(workspace.requiresBuild());
    assertTrue(Files.isSameFile(getTestDir("mavenWorkspace_customized").resolve("pom.xml"),
        workspace.getBuildFile()));
    assertTrue(Files.isSameFile(getTestDir("mavenWorkspace_customized").resolve("my_output_dir/artifact.jar"),
        workspace.findArtifact()));
  }

  @Test
  public void testBuild_mavenAndGradle() throws Exception {
    Workspace workspace = initBuilder("mavenAndGradleWorkspace").build();

    assertEquals(BuildTool.GRADLE, workspace.getBuildTool().get());
    assertTrue(workspace.requiresBuild());
    assertTrue(Files.isSameFile(getTestDir("mavenAndGradleWorkspace").resolve("build.gradle"),
        workspace.getBuildFile()));
    assertTrue(Files.isSameFile(getTestDir("mavenAndGradleWorkspace").resolve("build/libs/gradle_artifact.jar"),
        workspace.findArtifact()));
  }

  @Test
  public void testBuild_gradle() throws Exception {
    Workspace workspace = initBuilder("gradleWorkspace").build();

    assertEquals(BuildTool.GRADLE, workspace.getBuildTool().get());
    assertFalse(workspace.requiresBuild());
    assertTrue(workspace.isBuildable());
    assertTrue(Files.isSameFile(getTestDir("gradleWorkspace").resolve("build.gradle"),
        workspace.getBuildFile()));
    assertTrue(Files.isSameFile(getTestDir("gradleWorkspace").resolve("build/libs/artifact.jar"),
        workspace.findArtifact()));
  }

  @Test()
  public void testBuild_simple() throws Exception {
    Workspace workspace = initBuilder("simple").build();

    assertEquals(Optional.empty(), workspace.getBuildTool());
    assertFalse(workspace.requiresBuild());
    assertFalse(workspace.isBuildable());
    assertTrue(Files.isSameFile(getTestDir("simple").resolve("foo.war"),
        workspace.findArtifact()));
    try {
      workspace.getBuildFile();
      fail();
    } catch (FileNotFoundException e) {
      // Expect this to be caught.
      return;
    }
  }

  private Path getTestDir(String dirName) {
    return Paths.get(System.getProperty("user.dir"))
        .resolve("src/test/resources/testWorkspaces")
        .resolve(dirName);
  }

  private WorkspaceBuilder initBuilder(String dirName) {
    return new WorkspaceBuilder(appYamlParser, getTestDir(dirName));
  }
}
