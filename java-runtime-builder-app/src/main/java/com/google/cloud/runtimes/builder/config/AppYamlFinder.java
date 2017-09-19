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

import com.google.cloud.runtimes.builder.injection.ConfigYamlPath;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class AppYamlFinder {

  private final Logger logger = LoggerFactory.getLogger(AppYamlFinder.class);

  private static final List<String> DEFAULT_APP_YAML_LOCATIONS
      = ImmutableList.of("app.yaml", "src/main/appengine/app.yaml");
  private final Optional<String> providedConfigPath;

  /**
   * Constructs a new {@link AppYamlFinder}.
   *
   * @param providedConfigPath the expected path for a config file to be found
   */
  @Inject
  @VisibleForTesting
  public AppYamlFinder(@ConfigYamlPath Optional<String> providedConfigPath) {
    this.providedConfigPath = providedConfigPath;
  }

  /**
   * Search for app.yaml in a few expected paths within a directory
   *
   * @param searchDir a directory to search in
   * @return the path to the config file
   */
  public Optional<Path> findAppYamlFile(Path searchDir) {
    Preconditions.checkArgument(Files.isDirectory(searchDir));

    if (providedConfigPath.isPresent()) {
      // If a configYamlPath has been specified, don't look anywhere else for it. If it's not there,
      // fail loudly.
      Optional<Path> providedAppYaml = providedConfigPath
          .map(searchDir::resolve)
          .filter(this::isValidFilePath);

      if (!providedAppYaml.isPresent()) {
        logger.warn("A yaml configuration file was expected, but none was found at the provided "
            + "path: {}. Proceeding with default configuration values.", providedConfigPath.get());
      }
      return providedAppYaml;

    } else {
      // Search in the default locations for the config file. It's ok if we don't find anything.
      return DEFAULT_APP_YAML_LOCATIONS.stream()
          .map(pathName -> searchDir.resolve(pathName))
          .filter(this::isValidFilePath)
          .findFirst();
    }
  }

  private boolean isValidFilePath(Path path) {
    return Files.exists(path) && Files.isRegularFile(path);
  }

}
