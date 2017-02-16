package com.google.cloud.runtimes.builder.buildsteps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.runtimes.builder.exception.BuildStepException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.Test;

public class BuildStepTest {

  @Test
  public void testReadWriteMetadata() throws IOException, BuildStepException {
    Path workspace = getTempDir();

    String key = "test_key";
    String value = "test_value";
    BuildStep step1 = getTestBuildStep((metadata) -> {
      metadata.put(key, value);
    });

    BuildStep step2 = getTestBuildStep((metadata) -> {
      assertEquals(value, metadata.get(key));
      metadata.remove(key);
    });

    BuildStep step3 = getTestBuildStep((metadata) -> {
      assertTrue(metadata.isEmpty());
    });

    step1.run(workspace);
    step2.run(workspace);
    step3.run(workspace);
  }

  private Path getTempDir() throws IOException {
    return Files.createTempDirectory(null);
  }

  private BuildStep getTestBuildStep(Consumer<Map<String,String>> implementation) {
    return new BuildStep() {
      @Override
      protected void doBuild(Map<String, String> metadata) {
        implementation.accept(metadata);
      }
    };
  }
}
