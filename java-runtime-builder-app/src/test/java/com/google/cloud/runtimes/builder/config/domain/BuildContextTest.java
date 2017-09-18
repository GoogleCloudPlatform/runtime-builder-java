package com.google.cloud.runtimes.builder.config.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig.BetaSettings;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link BuildContext}.
 */
public class BuildContextTest {

  private Path workspace;
  private RuntimeConfig runtimeConfig;
  private boolean disableSourceBuild;

  private static final String DOCKER_IGNORE_PREAMBLE = "Dockerfile\n"
      + ".dockerignore\n";

  @Before
  public void before() throws IOException {
    // initialize to empty dir
    workspace = new TestWorkspaceBuilder().build();
    runtimeConfig = new RuntimeConfig();
    disableSourceBuild = false;
  }

  private BuildContext initBuildContext() {
    return new BuildContext(runtimeConfig, workspace, disableSourceBuild);
  }

  @Test
  public void testWriteDockerFilesWithEmptyBuffers() throws IOException {
    initBuildContext().writeDockerResources();
    assertEquals("", readFile(getDockerfile()));
    assertFalse(Files.exists(workspace.resolve(".dockerfile")));
  }

  @Test
  public void testWriteDockerFilesWithExistingDockerignore() throws IOException {
    String dockerIgnoreContents = "foo/**\nbar";
    workspace = new TestWorkspaceBuilder()
        .file(".dockerignore").withContents(dockerIgnoreContents).build()
        .build();

    BuildContext context = initBuildContext();
    String dockerIgnoreAppend = "more_paths";
    context.getDockerignore().appendLine(dockerIgnoreAppend);

    context.writeDockerResources();
    assertEquals("", readFile(getDockerfile()));
    assertEquals(dockerIgnoreContents + "\n" + DOCKER_IGNORE_PREAMBLE + dockerIgnoreAppend + "\n",
        readFile(getDockerIgnore()));
  }

  @Test
  public void testWriteDockerFilesWithExistingDockerignoreTerminalNewline() throws IOException {
    String commonIgnoreLine = "foo/**";
    String dockerIgnoreContents = commonIgnoreLine + "\nbar\n";
    workspace = new TestWorkspaceBuilder()
        .file(".dockerignore").withContents(dockerIgnoreContents).build()
        .build();

    BuildContext context = initBuildContext();
    String dockerIgnoreAppend = "more_paths";
    context.getDockerignore().appendLine(dockerIgnoreAppend);
    context.getDockerignore().appendLine(commonIgnoreLine);

    context.writeDockerResources();
    assertEquals("", readFile(getDockerfile()));

    String expectedDockerIgnore = dockerIgnoreContents + "\n"
        + DOCKER_IGNORE_PREAMBLE
        + dockerIgnoreAppend + "\n";
    assertEquals(expectedDockerIgnore, readFile(getDockerIgnore()));
  }

  @Test(expected = IllegalStateException.class)
  public void testWriteDockerFilesWithExistingDockerfile() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("Dockerfile").withContents("FROM foo\n").build()
        .build();

    BuildContext context = initBuildContext();
    context.writeDockerResources();
  }

  @Test
  public void testIsSourceBuildWithBuildScript() {
    runtimeConfig.setBuildScript("build script");
    assertTrue(initBuildContext().isSourceBuild());
  }

  @Test
  public void testIsSourceBuildWithPomXmlPresent() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .build();
    assertTrue(initBuildContext().isSourceBuild());
  }

  @Test
  public void testIsSourceBuildWithBuildGradlePresent() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("build.gradle").build()
        .build();
    assertTrue(initBuildContext().isSourceBuild());
  }

  @Test
  public void testIsSourceBuildDisabled() throws IOException {
    disableSourceBuild = true;
    workspace = new TestWorkspaceBuilder()
        .file("build.gradle").build()
        .build();
    assertFalse(initBuildContext().isSourceBuild());
  }

  @Test
  public void testGetBuildToolWithNone() throws IOException {
    assertFalse(initBuildContext().getBuildTool().isPresent());
  }

  @Test
  public void testGetBuildToolWithMavenAndGradle() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .file("build.gradle").build()
        .build();

    assertEquals(BuildTool.MAVEN,
        initBuildContext().getBuildTool().get());
  }

  @Test
  public void testGetBuildToolWithMaven() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("pom.xml").build()
        .build();

    assertEquals(BuildTool.MAVEN,
        initBuildContext().getBuildTool().get());
  }

  @Test
  public void testGetBuildToolWithGradle() throws IOException {
    workspace = new TestWorkspaceBuilder()
        .file("build.gradle").build()
        .build();

    assertEquals(BuildTool.GRADLE,
        initBuildContext().getBuildTool().get());
  }

  @Test
  public void testIsForceCompatRuntimeWithNullBetaSettings() {
    runtimeConfig.setBetaSettings(null);
    assertFalse(initBuildContext().isForceCompatRuntime());
  }

  @Test
  public void testIsForceCompatRuntimeWithBetaSettingsFalse() {
    BetaSettings betaSettings = new BetaSettings();
    betaSettings.setEnableAppEngineApis(false);
    runtimeConfig.setBetaSettings(betaSettings);
    assertFalse(initBuildContext().isForceCompatRuntime());
  }

  @Test
  public void testIsForceCompatRuntimeWithBetaSettingsTrue() {
    BetaSettings betaSettings = new BetaSettings();
    betaSettings.setEnableAppEngineApis(true);
    runtimeConfig.setBetaSettings(betaSettings);
    assertTrue(initBuildContext().isForceCompatRuntime());
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
