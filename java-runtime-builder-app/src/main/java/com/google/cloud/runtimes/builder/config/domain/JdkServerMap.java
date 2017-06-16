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

import com.google.cloud.runtimes.builder.injection.DefaultJdk;
import com.google.cloud.runtimes.builder.injection.DefaultServerType;
import com.google.cloud.runtimes.builder.injection.JdkServerMapArg;
import com.google.inject.Inject;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdkServerMap {

  private static final Logger log = LoggerFactory.getLogger(JdkServerMap.class);

  private static final String NO_SERVER_KEY = "no-server";

  private final Map<String, Map<String, String>> jdkServerMap;
  private final String defaultJdk;
  private final String defaultServerType;

  @Inject
  JdkServerMap(@JdkServerMapArg Map<String, Map<String, String>> jdkServerMap,
      @DefaultJdk String defaultJdk, @DefaultServerType String defaultServerType) {
    this.jdkServerMap = jdkServerMap;
    this.defaultJdk = defaultJdk;
    this.defaultServerType = defaultServerType;
  }

  public String lookupJdkImage(String jdk) {
    return getRuntimeMapForJdk(jdk).get(NO_SERVER_KEY);
  }

  public String lookupServerImage(String jdk, String serverType) {
    Map<String, String> runtimeMap = getRuntimeMapForJdk(jdk);
    if (serverType == null) {
      serverType = defaultServerType;
    }
    if (!runtimeMap.containsKey(serverType)) {
      log.error("Server type '{}' not recognized. Supported server types for jdk {} are: {}",
          serverType, jdk, runtimeMap.keySet());
      throw new IllegalArgumentException(String.format("Invalid server type: %s", serverType));
    }
    return runtimeMap.get(serverType);
  }

  private Map<String, String> getRuntimeMapForJdk(String jdk) {
    if (jdk == null) {
      jdk = defaultJdk;
    }
    if (!jdkServerMap.containsKey(jdk)) {
      log.error("JDK '{}' not recognized. Supported JDK values are: {}", jdk, jdkServerMap.keySet());
      throw new IllegalArgumentException(String.format("Invalid jdk: %s", jdk));
    }
    return jdkServerMap.get(jdk);
  }

}
