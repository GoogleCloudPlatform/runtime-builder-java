package com.google.cloud.runtimes.builder.config;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Unit tests for {@link AppYamlFinder}
 */
public class AppYamlFinderTest {

  @Test
  public void testAppYamlAtRootNoEnvVar() throws IOException {
    String yamlPath = "app.yaml";
    Path workspace = new TestWorkspaceBuilder()
        .file(yamlPath).build()
        .build();

    Optional<Path> result = new AppYamlFinder(Optional.empty()).findAppYamlFile(workspace);
    assertEquals(workspace.resolve(yamlPath), result.get());
  }

  @Test
  public void testAppYamlAtSrcMainNoEnvVar() throws IOException {
    String yamlPath = "src/main/appengine/app.yaml";
    Path workspace = new TestWorkspaceBuilder()
        .file(yamlPath).build()
        .build();

    Optional<Path> result = new AppYamlFinder(Optional.empty()).findAppYamlFile(workspace);
    assertEquals(workspace.resolve(yamlPath), result.get());
  }

  @Test
  public void testAppYamlWithEnvVar() throws IOException {
    String pathFromEnvVar = "somedir/arbitraryfile";
    Path workspace = new TestWorkspaceBuilder()
        .file("app.yaml").build()
        .file(pathFromEnvVar).build()
        .build();

    Optional<Path> result = new AppYamlFinder(Optional.of(pathFromEnvVar)).findAppYamlFile(workspace);
    assertEquals(workspace.resolve(pathFromEnvVar), result.get());
  }

  @Test
  public void testAppYamlWithInvalidEnvVar() throws IOException {
    String appYamlDefaultPath = "app.yaml";
    Path workspace = new TestWorkspaceBuilder()
        .file(appYamlDefaultPath).build()
        .build();

    Optional<Path> result = new AppYamlFinder(Optional.of("path/does/not/exist")).findAppYamlFile(workspace);
    assertEquals(Optional.empty(), result);
  }

  @Test
  public void testDirectoryAsAppYaml() throws IOException {
    Path workspace = new TestWorkspaceBuilder().build();
    Optional<Path> result = new AppYamlFinder(Optional.empty()).findAppYamlFile(workspace);
    assertFalse(result.isPresent());
  }

  @Test
  public void testAppYamlNotPresent() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("other.yaml").build()
        .build();
    Optional<Path> result = new AppYamlFinder(Optional.empty()).findAppYamlFile(workspace);
    assertFalse(result.isPresent());

  }

}
