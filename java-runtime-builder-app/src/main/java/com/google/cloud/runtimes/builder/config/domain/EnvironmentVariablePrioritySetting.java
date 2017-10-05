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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class EnvironmentVariablePrioritySetting {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public static String getEnv(String name) {
    return System.getenv(name);
  }

  /**
   * Checks for fields annotated with {@link SettingFromEnvironmentVariable} and checks if there is
   * an environment variable for each.
   */
  public void getEnvironmentVariableSettings() {
    for (Field field : this.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(SettingFromEnvironmentVariable.class)) {
        String fieldName = field.getName();
        String annotatedName = field.getAnnotation(SettingFromEnvironmentVariable.class).value();
        String envSetting = getEnv(annotatedName.isEmpty() ? fieldName : annotatedName);
        if (envSetting != null && !envSetting.isEmpty()) {
          setField(field, envSetting);
        }
      }
    }
  }

  private void setField(Field field, String envValue) {
    try {
      field.setAccessible(true);
      if (field.getType().equals(boolean.class)) {
        field.set(this, Boolean.valueOf(envValue));
      } else if (field.getType().equals(int.class)) {
        field.set(this, Integer.valueOf(envValue));
      } else if (field.getType().equals(long.class)) {
        field.set(this, Long.valueOf(envValue));
      } else if (field.getType().equals(float.class)) {
        field.set(this, Float.valueOf(envValue));
      } else if (field.getType().equals(double.class)) {
        field.set(this, Double.valueOf(envValue));
      } else if (field.getType().equals(String.class)) {
        field.set(this, envValue);
      } else {
        logger.warn("The environment variable could not be set as the requested type {}",
            field.getType().toString());
      }
    } catch (IllegalAccessException e) {
      logger.warn("Unable to set field from environment variable.", e);
    } finally {
      field.setAccessible(false);
    }
  }
}
