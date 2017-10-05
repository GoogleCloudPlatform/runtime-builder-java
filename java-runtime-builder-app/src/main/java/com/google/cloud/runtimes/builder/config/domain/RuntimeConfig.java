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
public final class RuntimeConfig extends EnvironmentVariablePrioritySetting {

  private static final String BUILD_SCRIPT_SETTING_NAME = "build_script";
  private static final String JETTY_QUICKSTART_SETTING_NAME = "jetty_quickstart";

  @SettingFromEnvironmentVariable
  private String jdk;

  @SettingFromEnvironmentVariable
  private String server;

  @SettingFromEnvironmentVariable(BUILD_SCRIPT_SETTING_NAME)
  private String buildScript;

  @SettingFromEnvironmentVariable
  private String artifact;

  @SettingFromEnvironmentVariable(JETTY_QUICKSTART_SETTING_NAME)
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

  @JsonProperty(BUILD_SCRIPT_SETTING_NAME)
  public void setBuildScript(String buildScript) {
    this.buildScript = buildScript;
  }

  public boolean getJettyQuickstart() {
    return jettyQuickstart;
  }

  @JsonProperty(JETTY_QUICKSTART_SETTING_NAME)
  public void setJettyQuickstart(boolean jettyQuickstart) {
    this.jettyQuickstart = jettyQuickstart;
  }
}
