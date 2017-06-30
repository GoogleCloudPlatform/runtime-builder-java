package com.google.cloud.runtimes.builder.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Unit tests for {@link AppYamlFinder}
 */
public class AppYamlFinderTest {

  @Test
  public void testAppYamlAtRootNoEnvVar() throws IOException, AppYamlNotFoundException {
    String yamlPath = "app.yaml";
    Path workspace = new TestWorkspaceBuilder()
        .file(yamlPath).build()
        .build();

    Optional<Path> result = new AppYamlFinder(null).findAppYamlFile(workspace);
    assertEquals(workspace.resolve(yamlPath), result.get());
  }

  @Test
  public void testAppYamlAtSrcMainNoEnvVar() throws IOException, AppYamlNotFoundException {
    String yamlPath = "src/main/appengine/app.yaml";
    Path workspace = new TestWorkspaceBuilder()
        .file(yamlPath).build()
        .build();

    Optional<Path> result = new AppYamlFinder(null).findAppYamlFile(workspace);
    assertEquals(workspace.resolve(yamlPath), result.get());
  }

  @Test
  public void testAppYamlWithEnvVar() throws IOException, AppYamlNotFoundException {
    String pathFromEnvVar = "somedir/arbitraryfile";
    Path workspace = new TestWorkspaceBuilder()
        .file("app.yaml").build()
        .file(pathFromEnvVar).build()
        .build();

    Optional<Path> result = new AppYamlFinder(pathFromEnvVar).findAppYamlFile(workspace);
    assertEquals(workspace.resolve(pathFromEnvVar), result.get());
  }

  @Test(expected = AppYamlNotFoundException.class)
  public void testAppYamlWithInvalidEnvVar() throws IOException, AppYamlNotFoundException {
    String appYamlDefaultPath = "app.yaml";
    Path workspace = new TestWorkspaceBuilder()
        .file(appYamlDefaultPath).build()
        .build();

    new AppYamlFinder("path/does/not/exist").findAppYamlFile(workspace);
  }

  @Test
  public void testDirectoryAsAppYaml() throws IOException, AppYamlNotFoundException {
    Path workspace = new TestWorkspaceBuilder().build();
    Optional<Path> result = new AppYamlFinder(null).findAppYamlFile(workspace);
    assertTrue(!result.isPresent());
  }

  @Test
  public void testAppYamlNotPresent() throws AppYamlNotFoundException, IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("other.yaml").build()
        .build();
    Optional<Path> result = new AppYamlFinder(null).findAppYamlFile(workspace);
    assertTrue(!result.isPresent());

  }

}
