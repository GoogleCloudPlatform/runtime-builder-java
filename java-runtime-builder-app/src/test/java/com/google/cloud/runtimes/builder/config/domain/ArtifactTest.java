package com.google.cloud.runtimes.builder.config.domain;

import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.EXPLODED_WAR;
import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.JAR;
import static com.google.cloud.runtimes.builder.config.domain.Artifact.ArtifactType.WAR;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ArtifactTest {

  private List<Path> invalidArtifacts = ImmutableList.of(
      Paths.get("foo.txt"),
      Paths.get("foo.java"),
      Paths.get("some_dir/"),
      Paths.get("WEB-INF/appengine-web.xml"),
      Paths.get("WEB-INF/web.xml")
  );

  @Test
  public void testNonExistentWar() {
    Path path = Paths.get("/foo/bar.war");
    assertTrue(Artifact.isAnArtifact(path));

    Artifact result = Artifact.fromPath(path);
    assertEquals(path, result.getPath());
    assertEquals(WAR, result.getType());
  }

  @Test
  public void testExistingWar() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo/bar.war").build()
        .build();

    Path path = workspace.resolve("/foo/bar.war");
    assertTrue(Artifact.isAnArtifact(path));

    Artifact result = Artifact.fromPath(path);
    assertEquals(path, result.getPath());
    assertEquals(WAR, result.getType());
  }

  @Test
  public void testJar() {
    Path path = Paths.get("foo.jar");
    assertTrue(Artifact.isAnArtifact(path));

    Artifact result = Artifact.fromPath(path);
    assertEquals(path, result.getPath());
    assertEquals(JAR, result.getType());
  }

  @Test
  public void testAppEngineExplodedWar() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("exploded-war/WEB-INF/appengine-web.xml").build()
        .file("exploded-war/WEB-INF/web.xml").build()
        .build();

    Path path = workspace.resolve("exploded-war");
    assertTrue(Artifact.isAnArtifact(path));
    assertFalse(Artifact.isAnArtifact(workspace));

    Artifact result = Artifact.fromPath(path);
    assertEquals(path, result.getPath());
    assertEquals(EXPLODED_WAR, result.getType());
  }

  @Test
  public void testExplodedWar() throws IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("exploded-war/WEB-INF/foo").build()
        .build();

    Path path = workspace.resolve("exploded-war");
    assertTrue(Artifact.isAnArtifact(path));
    assertFalse(Artifact.isAnArtifact(workspace));

    Artifact result = Artifact.fromPath(path);
    assertEquals(path, result.getPath());
    assertEquals(EXPLODED_WAR, result.getType());
  }

  @Test
  public void testInvalidArtifacts() throws IOException {
    for (Path p : invalidArtifacts) {
      assertFalse(Artifact.isAnArtifact(p));
    }
  }

  @Test
  public void testFromPathInvalidArtifact() {
    int thrown = 0;
    for (Path p : invalidArtifacts) {
      try {
        Artifact.fromPath(p);
      } catch (IllegalArgumentException e) {
        thrown++;
      }
    }

    assertEquals(invalidArtifacts.size(), thrown);
  }

}
