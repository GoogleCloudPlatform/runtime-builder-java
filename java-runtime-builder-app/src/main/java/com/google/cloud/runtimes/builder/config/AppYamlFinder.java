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

package com.google.cloud.runtimes.builder.config;

import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.cloud.runtimes.builder.injection.ConfigYamlPath;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;

public class AppYamlFinder {

  private final List<String> appYamlSearchPaths = new LinkedList<>(
      Arrays.asList("app.yaml", "src/main/appengine/app.yaml"));

  /**
   * Constructs a new {@link AppYamlFinder}.
   *
   * @param configYamlPath the expected path for a config file to be found
   */
  @Inject
  @VisibleForTesting
  public AppYamlFinder(@Nullable @ConfigYamlPath String configYamlPath) {
    // this config location takes priority, so insert it at front of the search list
    if (!Strings.isNullOrEmpty(configYamlPath)) {
      appYamlSearchPaths.add(0, configYamlPath);
    }
  }

  /**
   * Search for app.yaml in a few expected paths within a directory
   *
   * @param searchDir a directory to search in
   * @return the path to the config file
   * @throws AppYamlNotFoundException if no valid config file is found
   */
  public Path findAppYamlFile(Path searchDir) throws AppYamlNotFoundException {
    Preconditions.checkArgument(Files.isDirectory(searchDir));

    return appYamlSearchPaths.stream()
        .map(pathName -> searchDir.resolve(pathName))
        .filter(path -> Files.exists(path) && Files.isRegularFile(path))
        .findFirst()
        .orElseThrow(() -> new AppYamlNotFoundException("An app.yaml configuration file is "
            + "required, but was not found in the included files."));
  }

}
