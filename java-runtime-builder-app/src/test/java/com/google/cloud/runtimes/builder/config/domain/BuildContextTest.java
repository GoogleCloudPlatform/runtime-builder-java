package com.google.cloud.runtimes.builder.config.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;

import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit tests for {@link BuildContext}.
 */
public class BuildContextTest {

  private Path workspace;

  @Before
  public void before() throws IOException {
    // initialize to empty dir
    workspace = new TestWorkspaceBuilder().build();
  }

  @Test
  public void testWriteDockerFilesWithEmptyBuffers() throws IOException {
    new BuildContext(new RuntimeConfig(), workspace).writeDockerResources();
    assertEquals("", readFile(getDockerfile()));
    assertFalse(Files.exists(workspace.resolve(".dockerfile")));
  }

  @Test
  public void testWriteDockerFilesWithExistingDockerignore() throws IOException {
    String dockerIgnoreContents = "foo/**\nbar";
    workspace = new TestWorkspaceBuilder()
        .file(".dockerignore").withContents(dockerIgnoreContents).build()
        .build();

    BuildContext context = new BuildContext(new RuntimeConfig(), workspace);
    String dockerIgnoreAppend = "more_paths";
    context.getDockerignore().appendLine(dockerIgnoreAppend);

    context.writeDockerResources();
    assertEquals("", readFile(getDockerfile()));
    assertEquals(dockerIgnoreContents + "\n" + dockerIgnoreAppend + "\n",
        readFile(getDockerIgnore()));
  }

  @Test
  public void testWriteDockerFilesWithExistingDockerignoreTerminalNewline() throws IOException {
    String commonIgnoreLine = "foo/**";
    String dockerIgnoreContents = commonIgnoreLine + "\nbar\n";
    workspace = new TestWorkspaceBuilder()
        .file(".dockerignore").withContents(dockerIgnoreContents).build()
        .build();

    BuildContext context = new BuildContext(new RuntimeConfig(), workspace);
    String dockerIgnoreAppend = "more_paths";
    context.getDockerignore().appendLine(dockerIgnoreAppend);
    context.getDockerignore().appendLine(commonIgnoreLine);

    context.writeDockerResources();
    assertEquals("", readFile(getDockerfile()));
    assertEquals(dockerIgnoreContents + "\n" + dockerIgnoreAppend + "\n", readFile(getDockerIgnore()));
  }

  @Test(expected = IllegalStateException.class)
  public void testWriteDockerFilesWithExistingDockerfile() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("Dockerfile").withContents("FROM foo\n").build()
        .build();

    BuildContext context = new BuildContext(new RuntimeConfig(), workspace);
    context.writeDockerResources();
  }

  @Test
  public void testIsSourceBuildWithBuildScript() {
    RuntimeConfig config = new RuntimeConfig();
    config.setBuildScript("build script");
    assertTrue(new BuildContext(config, workspace).isSourceBuild());
  }

  @Test
  public void testIsSourceBuildWithPomXmlPresent() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .build();
    assertTrue(new BuildContext(new RuntimeConfig(), workspace).isSourceBuild());
  }

  @Test
  public void testIsSourceBuildWithBuildGradlePresent() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("build.gradle").build()
        .build();
    assertTrue(new BuildContext(new RuntimeConfig(), workspace).isSourceBuild());
  }

  @Test
  public void testFindArtifactsWithMultiple() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("foo.jar").build()
        .file("bar.JAR").build()
        .file("foo.war").build()
        .build();

    assertEquals(3, new BuildContext(new RuntimeConfig(), workspace).findArtifacts().size());
  }

  @Test
  public void testFindArtifactsWithNone() throws IOException {
    assertEquals(0, new BuildContext(new RuntimeConfig(), workspace).findArtifacts().size());
  }

  @Test
  public void testGetBuildToolWithNone() throws IOException {
    assertFalse(new BuildContext(new RuntimeConfig(), workspace).getBuildTool().isPresent());
  }

  @Test
  public void testGetBuildToolWithMavenAndGradle() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .file("build.gradle").build()
        .build();

    assertEquals(BuildTool.MAVEN,
        new BuildContext(new RuntimeConfig(), workspace).getBuildTool().get());
  }

  @Test
  public void testGetBuildToolWithMaven() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .build();

    assertEquals(BuildTool.MAVEN,
        new BuildContext(new RuntimeConfig(), workspace).getBuildTool().get());
  }

  @Test
  public void testGetBuildToolWithGradle() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("build.gradle").build()
        .build();

    assertEquals(BuildTool.GRADLE,
        new BuildContext(new RuntimeConfig(), workspace).getBuildTool().get());
  }

  private Path getDockerfile() {
    return workspace.resolve("Dockerfile");
  }

  private Path getDockerIgnore() {
    return workspace.resolve(".dockerignore");
  }

  private String readFile(Path file) throws IOException {
    if (!Files.exists(file)) {
      throw new FileNotFoundException(file + " not found!");
    }
    return new String(Files.readAllBytes(file));
  }
}
