package com.google.cloud.runtimes.builder.config;

import static org.junit.Assert.assertEquals;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    Path result = new AppYamlFinder(null).findAppYamlFile(workspace);
    assertEquals(workspace.resolve(yamlPath), result);
  }

  @Test
  public void testAppYamlAtSrcMainNoEnvVar() throws IOException, AppYamlNotFoundException {
    String yamlPath = "src/main/appengine/app.yaml";
    Path workspace = new TestWorkspaceBuilder()
        .file(yamlPath).build()
        .build();

    Path result = new AppYamlFinder(null).findAppYamlFile(workspace);
    assertEquals(workspace.resolve(yamlPath), result);
  }

  @Test
  public void testAppYamlWithEnvVar() throws IOException, AppYamlNotFoundException {
    String pathFromEnvVar = "somedir/arbitraryfile";
    Path workspace = new TestWorkspaceBuilder()
        .file("app.yaml").build()
        .file(pathFromEnvVar).build()
        .build();

    Path result = new AppYamlFinder(pathFromEnvVar).findAppYamlFile(workspace);
    assertEquals(workspace.resolve(pathFromEnvVar), result);
  }

  @Test
  public void testAppYamlWithInvalidEnvVar() throws IOException, AppYamlNotFoundException {
    String appYamlDefaultPath = "app.yaml";
    Path workspace = new TestWorkspaceBuilder()
        .file(appYamlDefaultPath).build()
        .build();

    Path result = new AppYamlFinder("path/does/not/exist").findAppYamlFile(workspace);
    assertEquals(workspace.resolve(appYamlDefaultPath), result);
  }

  @Test(expected = AppYamlNotFoundException.class)
  public void testDirectoryAsAppYaml() throws IOException, AppYamlNotFoundException {
    Path workspace = new TestWorkspaceBuilder().build();
    new AppYamlFinder(null).findAppYamlFile(workspace);
  }

  @Test(expected = AppYamlNotFoundException.class)
  public void testAppYamlNotPresent() throws AppYamlNotFoundException, IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("other.yaml").build()
        .build();
    new AppYamlFinder(null).findAppYamlFile(workspace);

  }

}
