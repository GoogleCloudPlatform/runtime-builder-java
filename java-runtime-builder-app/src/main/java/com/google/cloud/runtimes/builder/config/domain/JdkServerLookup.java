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

import java.util.Set;

public abstract class JdkServerLookup {

  protected static final String KEY_WILDCARD = "*";
  protected static final String KEY_DELIMITER = "|";

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

  /**
   * Lookup a JDK image for the given JDK name.
   *
   * @param jdk the key for the JDK. If {@code null}, a default will be used. If no
   *     valid JDK image is found, an {@link IllegalArgumentException} will be thrown.
   */
  public abstract String lookupJdkImage(String jdk);

  /**
   * Returns the set of available jdk settings.
   *
   * @return the set of available jdk settings.
   */
  public abstract Set<String> getAvailableJdks();

  /**
   * Lookup a server image for the given JDK name and server type.
   *
   * @param jdk the key for the JDK. If {@code null}, a default jdk will be used. If no
   *     valid JDK image is found, an {@link IllegalArgumentException} will be thrown.
   * @param serverType the type of the server. If {@code null}, a default server type will be used.
   *     If no valid server image is found, an {@link IllegalArgumentException} will be thrown.
   */
  public abstract String lookupServerImage(String jdk, String serverType);

  /**
   * Returns the set of available jdk and server setting pairs.
   *
   * @return the set of setting pairs.
   */
  public abstract Set<String> getAvailableJdkServerPairs();

  protected String buildServerMapKey(String jdk, String serverType) {
    String jdkKey = jdk == null ? KEY_WILDCARD : jdk;
    String serverTypeKey = serverType == null ? KEY_WILDCARD : serverType;

    return jdkKey + KEY_DELIMITER + serverTypeKey;
  }
}
