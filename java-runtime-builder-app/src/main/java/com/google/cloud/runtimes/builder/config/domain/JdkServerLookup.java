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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JdkServerLookup {

  public static final String KEY_WILDCARD = "*";
  public static final String KEY_DELIMITER = "|";

  private static final String KEY_DELIMITER_REGEX = "\\" + KEY_DELIMITER;

  private Map<String, String> serverRuntimeMap;
  private Map<String, String> jdkRuntimeMap;


  /**
   * Constructs a new {@link JdkServerLookup}. Validates for wildcard defaults existing.
   *
   * @param jdkRuntimeMap maps jdk config names to runtime images
   * @param serverRuntimeMap maps server and jdk config names to runtime images
   */
  public JdkServerLookup(String[] jdkRuntimeMap, String[] serverRuntimeMap) {
    Preconditions.checkNotNull(jdkRuntimeMap);
    Preconditions.checkNotNull(serverRuntimeMap);

    this.jdkRuntimeMap = getSettingsMap(jdkRuntimeMap, false);
    this.serverRuntimeMap = getSettingsMap(serverRuntimeMap, true);

    validate();
  }

  private static Map<String, String> getSettingsMap(String[] rawSettings, boolean server) {
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

          if (server) {
            String[] keyParts = split[0].split(KEY_DELIMITER_REGEX);
            if (keyParts.length != 2) {
              throw new IllegalArgumentException("Invalid server map key: '" + split[0] + "'. "
                  + "All server mapping keys must be formatted as: jdk" + KEY_DELIMITER
                  + "serverType");
            }
          }

          return split;
        })
        .collect(Collectors
            .toMap(a -> a[0].replaceAll("\\s+", ""), a -> a[1].replaceAll("\\s+", ""),
                (original, duplicate) -> original));
  }

  private void validate() {
    if (lookupJdkImage(KEY_WILDCARD) == null) {
      throw new IllegalArgumentException("Expected to find default (" + KEY_WILDCARD + ") key in "
          + "JDK runtime map.");
    }
    String serverMapKey = buildServerMapKey(null, null);
    if (lookupServerImage(null, null) == null) {
      throw new IllegalArgumentException("Expected to find default (" + serverMapKey + ") key in "
          + "server runtime map.");
    }
  }

  private String buildServerMapKey(String jdk, String serverType) {
    String jdkKey = jdk == null ? KEY_WILDCARD : jdk;
    String serverTypeKey = serverType == null ? KEY_WILDCARD : serverType;

    return jdkKey + KEY_DELIMITER + serverTypeKey;
  }

  /**
   * Returns the set of available jdk settings.
   *
   * @return the set of available jdk settings.
   */
  public Set<String> getAvailableJdks() {
    return this.jdkRuntimeMap.keySet()
        .stream()
        .filter((key) -> !key.contains(KEY_WILDCARD))
        .map(key -> "'" + key + "'")
        .collect(Collectors.toSet());
  }

  /**
   * Returns the set of available jdk and server setting pairs.
   *
   * @return the set of setting pairs.
   */
  public Set<String> getAvailableJdkServerPairs() {
    return this.serverRuntimeMap.keySet()
        .stream()
        .filter((key) -> !key.contains(KEY_WILDCARD))
        .map((key) -> key.split("\\" + KEY_DELIMITER))
        .map((split) -> split[0] + "/" + split[1])
        .collect(Collectors.toSet());
  }

  /**
   * Lookup a JDK image for the given JDK name.
   *
   * @param jdk the key for the JDK. If {@code null}, a default will be used. If no
   *     valid JDK image is found, an {@link IllegalArgumentException} will be thrown.
   */
  public String lookupJdkImage(String jdk) {
    String image;
    if (jdk == null) {
      image = this.jdkRuntimeMap.get(KEY_WILDCARD);
    } else {
      image = this.jdkRuntimeMap.get(jdk);
    }

    if (image == null) {
      throw new IllegalArgumentException(
          String.format("The provided runtime_config.jdk option '%s'"
                  + " is invalid for JAR deployments. Please use a supported jdk option: %s",
              Strings.nullToEmpty(jdk), getAvailableJdks()));
    }

    return image;
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
    String image = this.serverRuntimeMap.get(buildServerMapKey(jdk, serverType));

    if (image == null) {
      throw new IllegalArgumentException(String.format("The provided runtime_config.jdk and "
              + "runtime_config.server configuration (runtime_config.jdk: '%s', "
              + "runtime_config.server: '%s') is invalid for WAR "
              + "deployments. Please use a supported "
              + "jdk/server combination: %s",
          Strings.nullToEmpty(jdk), Strings.nullToEmpty(serverType),
          getAvailableJdkServerPairs()));
    }

    return image;
  }
}
