package com.google.cloud.runtimes.builder.buildsteps;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link ScriptExecutionBuildStep}
 */
public class ScriptExecutionBuildStepTest {

  @Mock private BuildContext buildContext;
  private StringBuilder dockerfileBuilder;

  @Before
  public void before() {
    dockerfileBuilder = new StringBuilder();

    MockitoAnnotations.initMocks(this);
    when(buildContext.getDockerfile()).thenReturn(dockerfileBuilder);
  }

  @Test
  public void testRun() throws BuildStepException {
    String buildCommand = "echo $VAR; cd /dir; mvn package";

    new ScriptExecutionBuildStep(buildCommand).run(buildContext);
    String dockerfileContents = dockerfileBuilder.toString();

    assertTrue(dockerfileContents.contains("RUN " + buildCommand + '\n'));
    assertTrue(dockerfileContents.contains("FROM " + ScriptExecutionBuildStep.BUILD_IMAGE
        + " as builder\n"));
  }

}
