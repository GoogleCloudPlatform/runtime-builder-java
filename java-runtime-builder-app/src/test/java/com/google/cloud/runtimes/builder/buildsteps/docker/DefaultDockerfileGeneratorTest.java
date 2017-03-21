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

package com.google.cloud.runtimes.builder.buildsteps.docker;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link DefaultDockerfileGenerator}.
 */
public class DefaultDockerfileGeneratorTest {

  private DefaultDockerfileGenerator generator;
  private String jarRuntime = "gcr.io/google-appengine/openjdk@sha256:12345";
  private String serverRuntime = "gcr.io/google-appengine/server@sha256:12345";

  @Before
  public void setup() throws IOException {
    MockitoAnnotations.initMocks(this);
    generator = new DefaultDockerfileGenerator(jarRuntime, serverRuntime);
  }

  @Test
  public void testGenerateOpenjdk() throws IOException {
    Path jar = Files.createTempFile( null, ".jar").toAbsolutePath();
    String result = generator.generateDockerfile(jar);
    assertTrue(result.contains("FROM " + jarRuntime));
    assertTrue(result.contains("ADD " + jar.toString()));
  }

  @Test
  public void testGenerateJetty() throws IOException {
    Path war = Files.createTempFile( null, ".war").toAbsolutePath();
    String result = generator.generateDockerfile(war);
    assertTrue(result.contains("FROM " + serverRuntime));
    assertTrue(result.contains("ADD " + war.toString()));
  }

}
