package com.google.cloud.runtimes.builder.buildsteps.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.google.cloud.runtimes.builder.TestUtils;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepMetadataConstants;
import com.google.cloud.runtimes.builder.exception.BuildToolInvokerException;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link MavenBuildStep}.
 */
public class MavenBuildStepTest {

  private MavenBuildStep mavenBuildStep;
  private Map<String, String> buildStepMetadata;

  // TODO
//  @Before
//  public void setup() {
//    MockitoAnnotations.initMocks(this);
//    mavenBuildStep = new MavenBuildStep(mavenInvoker);
//    buildStepMetadata = new HashMap<>();
//  }
//
//  @Test(expected = IllegalArgumentException.class)
//  public void test_noPomFile() throws IOException, BuildStepException {
//    Path dir = Files.createTempDir().toPath();
//    dir.resolve("notpom.xml").toFile().createNewFile();
//    mavenBuildStep.doBuild(dir, buildStepMetadata);
//  }
//
//  @Test
//  public void testBuildToolInvokerException() throws BuildToolInvokerException, IOException {
//    BuildToolInvokerException thrown = new BuildToolInvokerException("Some message");
//    doThrow(thrown).when(mavenInvoker).invoke(any(), any());
//
//    try {
//      mavenBuildStep.doBuild(getMavenProjectDir(), buildStepMetadata);
//    } catch (BuildStepException e) {
//      assertEquals(thrown, e.getCause());
//      return;
//    }
//    fail();
//  }
//
//  @Test
//  public void testSetMetadata() throws BuildStepException {
//    Path testDir = getMavenProjectDir();
//    mavenBuildStep.doBuild(testDir, buildStepMetadata);
//
//    assertEquals("target/",
//        buildStepMetadata.get(BuildStepMetadataConstants.BUILD_ARTIFACT_PATH));
//  }
//
//  private Path getMavenProjectDir() {
//    return TestUtils.getTestDataDir("mavenWorkspace");
//  }

}
