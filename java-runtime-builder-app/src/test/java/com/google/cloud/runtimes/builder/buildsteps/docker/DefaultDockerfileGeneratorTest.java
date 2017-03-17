/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.google.cloud.runtimes.builder.buildsteps.docker;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link DefaultDockerfileGenerator}.
 */
public class DefaultDockerfileGeneratorTest {

  private DefaultDockerfileGenerator generator;
  @Mock private Properties runtimeDigestsProperties;

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    generator = new DefaultDockerfileGenerator(runtimeDigestsProperties);
  }

  @Test
  public void testGenerateOpenjdk() throws IOException {
    Path jar = Files.createTempFile( null, ".jar").toAbsolutePath();
    testGenerateForBaseRuntime("openjdk", jar);
  }

  @Test
  public void testGenerateJetty() throws IOException {
    Path war = Files.createTempFile( null, ".war").toAbsolutePath();
    testGenerateForBaseRuntime("jetty", war);
  }

  private void testGenerateForBaseRuntime(String runtimeName, Path artifactToDeploy) {
    String digest = "sha256:123456789";

    when(runtimeDigestsProperties.getProperty(runtimeName)).thenReturn(digest);
    String result = generator.generateDockerfile(artifactToDeploy);
    assertTrue(result.contains("FROM gcr.io/google_appengine/" + runtimeName + "@" + digest));
    assertTrue(result.contains("ADD " + artifactToDeploy.toString()));
  }

}
