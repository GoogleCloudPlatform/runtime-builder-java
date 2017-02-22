package com.google.cloud.runtimes.builder.buildsteps.gradle;

import com.google.cloud.runtimes.builder.TestUtils;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepMetadataConstants;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link GradleBuildStep}
 */
public class GradleBuildStepTest {

  private GradleBuildStep gradleBuildStep;
  private Map<String, String> metadata;

  @Before
  public void setup() {
    metadata = new HashMap<>();
    gradleBuildStep = new GradleBuildStep();
  }

  @Test
  public void test_withGradleWrapper() throws BuildStepException {
    Path path = TestUtils.getTestDataDir("gradleWorkspaceWithWrapper");
    gradleBuildStep.doBuild(path, metadata);
    // TODO somehow assert that the wrapper was called
  }

}
