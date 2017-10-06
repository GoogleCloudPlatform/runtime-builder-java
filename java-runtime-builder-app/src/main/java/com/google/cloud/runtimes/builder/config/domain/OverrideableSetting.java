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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OverrideableSetting {

  /**
   * Gets the settings name from a settings field.
   */
  public static String getSettingName(Field field) {
    String fieldName = field.getName();
    String annotatedName = field.getAnnotation(OverrideSetting.class).value();
    return annotatedName.isEmpty() ? fieldName : annotatedName;
  }

  /**
   * Gets a list a of settings fields from a given settings class type.
   *
   * @param settingsType the settings class type to check.
   * @return the list of setting fields.
   */
  public static List<Field> getOverridableFields(Class settingsType) {
    List<Field> fields = new ArrayList<>();
    for (Field field : settingsType.getDeclaredFields()) {
      if (field.isAnnotationPresent(OverrideSetting.class)) {
        fields.add(field);
      }
    }
    return fields;
  }

  /**
   * Checks for fields annotated with {@link OverrideSetting} and checks if there is an environment
   * variable for each.
   */
  public void applyOverrideSettings(Map<String, Object> commandLineOverrideSettings)
      throws IOException {
    for (Field field : getOverridableFields(this.getClass())) {
      Object envSetting = commandLineOverrideSettings.get(getSettingName(field));
      if (envSetting != null) {
        setField(field, envSetting);
      }
    }
  }

  private void setField(Field field, Object envValue) throws IOException {
    try {
      field.setAccessible(true);
      field.set(this, envValue);
    } catch (IllegalAccessException e) {
      throw new IOException("Unable to set field from environment variable.", e);
    } finally {
      field.setAccessible(false);
    }
  }
}
