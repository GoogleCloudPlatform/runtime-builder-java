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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JdkServerLookup {

  private static final Logger log = LoggerFactory.getLogger(JdkServerLookup.class);

  private final Map<String, JdkServerMapEntry> jdkServerMap;
  private final String defaultJdk;
  private final Map<String, String> serverRuntimeMap;

  /**
   * Constructs a new {@link JdkServerLookup}.
   *
   * @param jdkServerMap encodes the mappings between jdk, server and runtime names
   * @param defaultJdk the jdk to use for lookups if none is provided. It must be a key in the
   *     {@code jdkServerMap}.
   */
  public JdkServerLookup(Map<String, JdkServerMapEntry> jdkServerMap, String defaultJdk) {
    this.jdkServerMap = jdkServerMap;
    this.defaultJdk = defaultJdk;

    // Build map of server name to runtimes
    serverRuntimeMap = new HashMap<>();
    for (JdkServerMapEntry jdkServerMapping : jdkServerMap.values()) {
      for (Entry<String, String> serverImageEntry : jdkServerMapping.getServerImages().entrySet()) {
        // Insert into global map of server names to runtimes. Each should be globally unique
        if (serverRuntimeMap.containsKey(serverImageEntry.getKey())) {
          throw new IllegalArgumentException("Found duplicate mapping for server name "
              + serverImageEntry.getKey());
        }
        serverRuntimeMap.put(serverImageEntry.getKey(), serverImageEntry.getValue());
      }
    }

    validate();
  }

  /*
   * Ensure defaults are present in the lookup map
   */
  private void validate() {
    Preconditions.checkArgument(jdkServerMap.containsKey(defaultJdk));

    for (JdkServerMapEntry entry : jdkServerMap.values()) {
      Map<String, String> serverRuntimeMap = entry.getServerImages();
      Preconditions.checkArgument(serverRuntimeMap.containsKey(entry.getDefaultServer()));
    }
  }

  /**
   * Lookup a JDK image for the given JDK name.
   *
   * @param jdk the key for the JDK. If {@code null}, the {@code defaultJdk} will be used. If no
   *     valid JDK image is found, an {@link IllegalArgumentException} will be thrown.
   */
  public String lookupJdkImage(String jdk) {
    return getRuntimeMapEntryForJdk(jdk).getJdkImage();
  }

  /**
   * Lookup a server image for the given JDK name and server type.
   *
   * @param jdk the key for the JDK. If {@code null}, the {@code defaultJdk} will be used. If no
   *     valid JDK image is found, an {@link IllegalArgumentException} will be thrown.
   * @param serverType the type of the server. If {@code null}, the {@code defaultServerType} will
   *     be used. If no valid server image is found, an {@link IllegalArgumentException} will be
   *     thrown.
   */
  public String lookupServerImage(String jdk, String serverType) {
    if (jdk == null && serverType != null) {
      // lookup image by server name only
      if (!serverRuntimeMap.containsKey(serverType)) {
        log.error("Server type '{}' not recognized. Supported server types for jdk {} are: {}",
            serverType, jdk, serverRuntimeMap.keySet());
        throw new IllegalArgumentException(String.format("Invalid server type: %s", serverType));
      }
      return serverRuntimeMap.get(serverType);

    } else {
      // perform lookup with default jdk
      JdkServerLookup.JdkServerMapEntry runtimeMap = getRuntimeMapEntryForJdk(jdk);
      if (serverType == null) {
        serverType = runtimeMap.getDefaultServer();
      }
      return runtimeMap.getServerImages().get(serverType);
    }
  }

  private JdkServerLookup.JdkServerMapEntry getRuntimeMapEntryForJdk(String jdk) {
    if (jdk == null) {
      jdk = defaultJdk;
    }
    if (!jdkServerMap.containsKey(jdk)) {
      log.error("JDK '{}' not recognized. Supported JDK values are: {}", jdk,
          jdkServerMap.keySet());
      throw new IllegalArgumentException(String.format("Invalid jdk: %s", jdk));
    }
    return jdkServerMap.get(jdk);
  }

  public static class JdkServerMapEntry {
    private String jdkImage;
    private String defaultServer;
    private Map<String, String> serverImages;

    public String getJdkImage() {
      return jdkImage;
    }

    @JsonProperty("jdk_image")
    public void setJdkImage(String jdkImage) {
      this.jdkImage = jdkImage;
    }

    public String getDefaultServer() {
      return defaultServer;
    }

    @JsonProperty("default_server")
    public void setDefaultServer(String defaultServer) {
      this.defaultServer = defaultServer;
    }

    public Map<String, String> getServerImages() {
      return serverImages;
    }

    @JsonProperty("server_images")
    public void setServerImages(Map<String, String> serverImages) {
      this.serverImages = serverImages;
    }
  }

}
