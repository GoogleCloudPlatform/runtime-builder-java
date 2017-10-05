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
public class AppYaml {

  private RuntimeConfig runtimeConfig = new RuntimeConfig();
  private BetaSettings betaSettings = new BetaSettings();

  /**
   * Checks environment variables and overwrites any existing settings in this object.
   */
  public void getSettingsFromEnvironment() {
    runtimeConfig.getEnvironmentVariableSettings();
    betaSettings.getEnvironmentVariableSettings();
  }

  @JsonProperty("runtime_config")
  public RuntimeConfig getRuntimeConfig() {
    return runtimeConfig;
  }

  public void setRuntimeConfig(RuntimeConfig runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
  }

  @JsonProperty("beta_settings")
  public BetaSettings getBetaSettings() {
    return betaSettings;
  }

  public void setBetaSettings(BetaSettings betaSettings) {
    this.betaSettings = betaSettings;
  }
}
