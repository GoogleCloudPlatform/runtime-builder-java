/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.runtimes.builder.buildsteps.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

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
      protected void doBuild(Path directory, Map<String, String> metadata)
          throws BuildStepException {
        implementation.accept(metadata);
      }
    };
  }
}
