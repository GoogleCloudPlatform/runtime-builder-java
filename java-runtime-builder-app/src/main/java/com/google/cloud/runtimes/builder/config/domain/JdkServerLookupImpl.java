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

package com.google.cloud.runtimes.builder.config.domain;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JdkServerLookupImpl extends JdkServerLookup {

  private static final String KEY_DELIMITER_REGEX = "\\" + KEY_DELIMITER;

  private final Map<String, String> serverRuntimeMap = new HashMap<>();
  private final Map<String, String> jdkRuntimeMap = new HashMap<>();

  /**
   * Constructs a new {@link JdkServerLookup}.
   *
   * @param jdkRuntimeMap maps jdk config names to runtime images
   * @param serverRuntimeMap maps server and jdk config names to runtime images
   */
  public JdkServerLookupImpl(Map<String, String> jdkRuntimeMap,
      Map<String, String> serverRuntimeMap) {
    Preconditions.checkNotNull(jdkRuntimeMap);
    Preconditions.checkNotNull(serverRuntimeMap);

    for (String key : jdkRuntimeMap.keySet()) {
      this.jdkRuntimeMap.put(key.trim(), jdkRuntimeMap.get(key).trim());
    }

    for (String key : serverRuntimeMap.keySet()) {
      String[] keyParts = key.split(KEY_DELIMITER_REGEX);
      if (keyParts.length != 2) {
        throw new IllegalArgumentException("Invalid server map key: '" + key + "'. "
            + "All server mapping keys must be formatted as: jdk" + KEY_DELIMITER + "serverType");
      }
      this.serverRuntimeMap
          .put(buildServerMapKey(keyParts[0].trim(), keyParts[1].trim()),
              serverRuntimeMap.get(key).trim());
    }
  }

  /**
   * Constructs a new {@link JdkServerLookup}.
   *
   * @param jdkRuntimeMap maps jdk config names to runtime images
   * @param serverRuntimeMap maps server and jdk config names to runtime images
   */
  public JdkServerLookupImpl(String[] jdkRuntimeMap, String[] serverRuntimeMap) {
    this(getSettingsMap(jdkRuntimeMap), getSettingsMap(serverRuntimeMap));
  }

  private static Map<String, String> getSettingsMap(String[] rawSettings) {
    if (rawSettings == null) {
      return Collections.emptyMap();
    }
    return Arrays.stream(rawSettings)
        .map(s -> {
          String[] split = s.split("=");
          // make sure mappings are formatted correctly
          if (split.length != 2) {
            throw new IllegalArgumentException("Invalid mapping: '" + s + "'. "
                + "All jdk/server mappings must be formatted as: KEY=VAL");
          }
          return split;
        })
        .collect(Collectors.toMap(a -> a[0], a -> a[1]));
  }

  @Override
  public Map<String, String> getJdkRuntimeMap() {
    return this.jdkRuntimeMap;
  }

  @Override
  public Map<String, String> getServerRuntimeMap() {
    return this.serverRuntimeMap;
  }
}
