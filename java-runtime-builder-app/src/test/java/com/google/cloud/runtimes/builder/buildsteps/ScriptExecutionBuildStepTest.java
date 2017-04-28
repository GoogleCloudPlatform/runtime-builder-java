package com.google.cloud.runtimes.builder.buildsteps;

import static junit.framework.TestCase.assertEquals;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * Unit tests for {@link ScriptExecutionBuildStep}
 */
public class ScriptExecutionBuildStepTest {

  @Test
  public void testBuildCommand() {
    String buildCommand = "echo $VAR; cd /dir; mvn package";
    List<String> expected = Arrays.asList("/bin/bash", "-c", buildCommand);

    List<String> cmd = new ScriptExecutionBuildStep(buildCommand)
        .getBuildCommand(Paths.get("FAKEDIR"));

    assertEquals(expected.size(), cmd.size());
    for (int i=0; i < expected.size(); i++) {
      assertEquals(expected.get(i), cmd.get(i));
    }
  }

}
