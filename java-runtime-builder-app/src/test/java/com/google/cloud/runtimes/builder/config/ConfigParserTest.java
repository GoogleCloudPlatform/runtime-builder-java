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

package com.google.cloud.runtimes.builder.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests for {@link ConfigParser}
 */
public class ConfigParserTest {

  private ConfigParser configParser;

  @Before
  public void setup() {
    configParser = new ConfigParser();
  }

  @Test
  public void testParse_allFields() throws IOException {
    String config = "{"
        + "\"entrypoint\": \"java -jar my-app.jar\","
        + "\"packages\": ["
        + "  \"ffmpeg\","
        + "  \"imagemagick=1.1.2\""
        + "],"
        + "\"build_script\": \"./gradlew build myThing\","
        + "\"artifact\": \"my-app.jar\""
        + "}";
    RuntimeConfig result = configParser.parse(config);
    assertEquals("./gradlew build myThing", result.getBuildScript());
    assertEquals("my-app.jar", result.getArtifact());
    assertEquals("java -jar my-app.jar", result.getEntrypoint());

    assertEquals(2, result.getPackages().size());
    assertEquals("ffmpeg", result.getPackages().get(0));
    assertEquals("imagemagick=1.1.2", result.getPackages().get(1));
  }

  @Test
  public void testParse_empty() throws IOException {
    String config = "{}";
    RuntimeConfig result = configParser.parse(config);
    assertNotNull(result);
  }

}
