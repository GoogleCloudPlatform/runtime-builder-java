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
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JdkServerLookup {

  private static final String KEY_DELIMITER = "|";
  private static final String KEY_WILDCARD = "*";
  private static final Logger log = LoggerFactory.getLogger(JdkServerLookup.class);

  private final Map<String, String> serverRuntimeMap;
  private final Map<String, String> jdkRuntimeMap;

  /**
   * Constructs a new {@link JdkServerLookup}.
   *
   * @param jdkRuntimeMap maps jdk config names to runtime images
   * @param serverRuntimeMap maps server and jdk config names to runtime images
   */
  public JdkServerLookup(Map<String, String> jdkRuntimeMap, Map<String, String> serverRuntimeMap) {
    Preconditions.checkNotNull(jdkRuntimeMap);
    Preconditions.checkNotNull(serverRuntimeMap);

    this.jdkRuntimeMap = jdkRuntimeMap;
    this.serverRuntimeMap = serverRuntimeMap;

    validate();
  }

  private void validate() {
    // make sure each map contains default keys
    if (!jdkRuntimeMap.containsKey(KEY_WILDCARD)) {
      throw new IllegalArgumentException("Expected to find default (" + KEY_WILDCARD + ") key in "
          + "JDK runtime map.");
    }
    String serverMapKey = buildServerMapKey(null, null);
    if (!serverRuntimeMap.containsKey(serverMapKey)) {
      throw new IllegalArgumentException("Expected to find default (" + serverMapKey + ") key in "
          + "server runtime map.");
    }
  }

  /**
   * Lookup a JDK image for the given JDK name.
   *
   * @param jdk the key for the JDK. If {@code null}, a default will be used. If no
   *     valid JDK image is found, an {@link IllegalArgumentException} will be thrown.
   */
  public String lookupJdkImage(String jdk) {
    if (jdk == null) {
      return jdkRuntimeMap.get(KEY_WILDCARD);
    }

    if (!jdkRuntimeMap.containsKey(jdk)) {
      throw new IllegalArgumentException(String.format("The provided runtime_config.jdk option '%s'"
          + " is invalid for JAR deployments. Please use a supported jdk option: %s",
          Strings.nullToEmpty(jdk), getAvailableJdks()));
    }
    return jdkRuntimeMap.get(jdk);
  }

  private List<String> getAvailableJdks() {
    return jdkRuntimeMap.keySet()
        .stream()
        .filter((key) -> !key.contains(KEY_WILDCARD))
        .map(key -> "'" + key + "'")
        .collect(Collectors.toList());
  }

  private String buildServerMapKey(String jdk, String serverType) {
    String jdkKey = jdk == null ? KEY_WILDCARD : jdk;
    String serverTypeKey = serverType == null ? KEY_WILDCARD : serverType;

    return jdkKey + KEY_DELIMITER + serverTypeKey;
  }

  /**
   * Lookup a server image for the given JDK name and server type.
   *
   * @param jdk the key for the JDK. If {@code null}, a default jdk will be used. If no
   *     valid JDK image is found, an {@link IllegalArgumentException} will be thrown.
   * @param serverType the type of the server. If {@code null}, a default server type will be used.
   *     If no valid server image is found, an {@link IllegalArgumentException} will be thrown.
   */
  public String lookupServerImage(String jdk, String serverType) {
    String key = buildServerMapKey(jdk, serverType);

    if (!serverRuntimeMap.containsKey(key)) {
      throw new IllegalArgumentException(String.format("The provided runtime_config.jdk and "
          + "runtime_config.server configuration (runtime_config.jdk: '%s', "
          + "runtime_config.server: '%s') is invalid for WAR deployments. Please use a supported "
          + "jdk/server combination: %s", Strings.nullToEmpty(jdk), Strings.nullToEmpty(serverType),
          getAvailableJdkServerPairs()));
    }
    return serverRuntimeMap.get(key);
  }

  private List<String> getAvailableJdkServerPairs() {
    return serverRuntimeMap.keySet()
        .stream()
        .filter((key) -> !key.contains(KEY_WILDCARD))
        .map((key) -> key.split("\\" + KEY_DELIMITER))
        .map((split) -> split[0] + "/" + split[1])
        .collect(Collectors.toList());
  }

}
