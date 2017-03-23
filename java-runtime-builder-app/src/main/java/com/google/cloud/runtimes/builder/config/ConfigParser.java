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


import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles parsing of JSON.
 */
public class ConfigParser {

  // this is OK because ObjectMapper is thread-safe
  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  /**
   * Parse JSON configuration from a string.
   *
   * @param jsonString the string to parse
   * @throws IOException if there was a transient error while parsing the string
   */
  public RuntimeConfig parse(String jsonString) throws IOException {
    try (InputStream in = new ByteArrayInputStream(jsonString.getBytes())) {
      return objectMapper.readValue(in, RuntimeConfig.class);
    }
  }
}
