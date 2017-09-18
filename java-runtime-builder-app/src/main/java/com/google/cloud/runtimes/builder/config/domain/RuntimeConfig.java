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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuntimeConfig {

  private String jdk;
  private String server;
  private String buildScript;
  private String artifact;
  private boolean jettyQuickstart;

  public String getJdk() {
    return jdk;
  }

  public void setJdk(String jdk) {
    this.jdk = jdk;
  }

  public String getArtifact() {
    return artifact;
  }

  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public String getBuildScript() {
    return buildScript;
  }

  @JsonProperty("build_script")
  public void setBuildScript(String buildScript) {
    this.buildScript = buildScript;
  }

  public boolean getJettyQuickstart() {
    return jettyQuickstart;
  }

  @JsonProperty("jetty_quickstart")
  public void setJettyQuickstart(boolean jettyQuickstart) {
    this.jettyQuickstart = jettyQuickstart;
  }
}
