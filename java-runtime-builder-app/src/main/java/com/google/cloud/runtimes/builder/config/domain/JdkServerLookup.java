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

import com.google.common.base.Strings;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class JdkServerLookup {

  protected static final String KEY_DELIMITER = "|";
  protected static final String KEY_WILDCARD = "*";

  /**
   * Constructor that optionally validates the settings to ensure wildcards are present.
   * @param doValidate if true will check that the wildcard settings are present.
   */
  public JdkServerLookup(boolean doValidate) {
    if (doValidate) {
      validate();
    }
  }

  protected JdkServerLookup() {
  }

  private void validate() {
    if (!getJdkRuntimeMap().containsKey(KEY_WILDCARD)) {
      throw new IllegalArgumentException("Expected to find default (" + KEY_WILDCARD + ") key in "
          + "JDK runtime map.");
    }
    String serverMapKey = buildServerMapKey(null, null);
    if (!getServerRuntimeMap().containsKey(serverMapKey)) {
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
    Map<String, String> jdkRuntimeMap = getJdkRuntimeMap();
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
    return getJdkRuntimeMap().keySet()
        .stream()
        .filter((key) -> !key.contains(KEY_WILDCARD))
        .map(key -> "'" + key + "'")
        .collect(Collectors.toList());
  }

  public abstract Map<String, String> getJdkRuntimeMap();

  /**
   * Lookup a server image for the given JDK name and server type.
   *
   * @param jdk the key for the JDK. If {@code null}, a default jdk will be used. If no
   *     valid JDK image is found, an {@link IllegalArgumentException} will be thrown.
   * @param serverType the type of the server. If {@code null}, a default server type will be used.
   *     If no valid server image is found, an {@link IllegalArgumentException} will be thrown.
   */
  public String lookupServerImage(String jdk, String serverType) {
    Map<String, String> serverRuntimeMap = getServerRuntimeMap();
    String key = buildServerMapKey(jdk, serverType);

    if (!serverRuntimeMap.containsKey(key)) {
      throw new IllegalArgumentException(String.format("The provided runtime_config.jdk and "
              + "runtime_config.server configuration (runtime_config.jdk: '%s', "
              + "runtime_config.server: '%s') is invalid for WAR "
              + "deployments. Please use a supported "
              + "jdk/server combination: %s",
          Strings.nullToEmpty(jdk), Strings.nullToEmpty(serverType),
          getAvailableJdkServerPairs()));
    }
    return serverRuntimeMap.get(key);
  }

  private List<String> getAvailableJdkServerPairs() {
    return getServerRuntimeMap().keySet()
        .stream()
        .filter((key) -> !key.contains(KEY_WILDCARD))
        .map((key) -> key.split("\\" + KEY_DELIMITER))
        .map((split) -> split[0] + "/" + split[1])
        .collect(Collectors.toList());
  }

  public abstract Map<String, String> getServerRuntimeMap();

  protected String buildServerMapKey(String jdk, String serverType) {
    String jdkKey = jdk == null ? KEY_WILDCARD : jdk;
    String serverTypeKey = serverType == null ? KEY_WILDCARD : serverType;

    return jdkKey + KEY_DELIMITER + serverTypeKey;
  }
}
