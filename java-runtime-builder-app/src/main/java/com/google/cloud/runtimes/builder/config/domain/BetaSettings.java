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
public final class BetaSettings extends EnvironmentVariablePrioritySetting {

  private static final String ENABLE_APP_ENGINE_APIS_SETTING_NAME = "enable_app_engine_apis";

  @SettingFromEnvironmentVariable(ENABLE_APP_ENGINE_APIS_SETTING_NAME)
  private boolean enableAppEngineApis = false;

  public boolean isEnableAppEngineApis() {
    return enableAppEngineApis;
  }

  @JsonProperty(ENABLE_APP_ENGINE_APIS_SETTING_NAME)
  public void setEnableAppEngineApis(boolean enableAppEngineApis) {
    this.enableAppEngineApis = enableAppEngineApis;
  }
}