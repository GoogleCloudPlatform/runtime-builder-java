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

package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.cloud.runtimes.builder.injection.RootModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Top-level class for executing from the command line.
 */
public class Application {

  private static final String JDK_RUNTIMES_MAPPINGS = "--jdk-runtimes-map";
  private static final String SERVER_RUNTIMES_MAPPINGS = "--server-runtimes-map";

  /**
   * Main method for invocation from the command line. Handles parsing of command line options.
   */
  public static void main(String[] args)
      throws BuildStepException, IOException, AppYamlNotFoundException {
    RootModule module;
    try {
      module = configureApplicationModule(args);
    } catch (IllegalArgumentException e) {
      printUsage();
      throw e;
    }

    Path workspaceDir = Paths.get(System.getProperty("user.dir"));
    // perform dependency injection, initialize the application
    Injector injector = Guice.createInjector(module);
    injector.getInstance(BuildPipeline.class).build(workspaceDir);
  }

  /**
   * Parses command-line arguments and configures a module for the application.
   */
  @VisibleForTesting
  static RootModule configureApplicationModule(String[] args) {
    Map<String, String> jdkMap = null;
    Map<String, String> serverMap = null;

    int index = 0;
    while (index < args.length) {
      String arg = args[index];

      String currentMapFlag = arg;
      if (!isMapFlag(currentMapFlag)) {
        throw new IllegalArgumentException("Expected program argument '" + arg + "' to be one of: {"
            + JDK_RUNTIMES_MAPPINGS + ", " + SERVER_RUNTIMES_MAPPINGS + "}");
      }

      // advance through all of the entries in the current map
      int startMapEntries = index + 1;
      while (++index < args.length && !isMapFlag(args[index])) {
        // do nothing
      }
      int endMapEntries = index - 1;

      if (endMapEntries - startMapEntries < 0) {
        throw new IllegalArgumentException("Expected one or more map entry arguments following '"
            + currentMapFlag + "'");
      }

      // build the map and assign to correct variable name
      Map<String, String> map = readMapFromArgs(args, startMapEntries, endMapEntries);
      if (isJdkMapFlag(currentMapFlag)) {
        jdkMap = map;
      } else {
        serverMap = map;
      }
    }

    // verify that we have read in both required maps
    if (jdkMap == null || serverMap == null) {
      throw new IllegalArgumentException("Expected both " + JDK_RUNTIMES_MAPPINGS + " and "
          + SERVER_RUNTIMES_MAPPINGS + " to be provided as program arguments.");
    }

    return new RootModule(jdkMap, serverMap);
  }

  private static Map<String, String> readMapFromArgs(String[] args, int startIndex, int endIndex) {
    Map<String, String> map = new HashMap<>();

    for (int i = startIndex; i <= endIndex; i++) {
      String arg = args[i];
      String[] split = arg.split("=");
      if (split.length != 2) {
        throw new IllegalArgumentException("Could not parse arg '" + arg + "'. "
            + "Map entry arguments must be of the form: 'KEY=VAL'");
      }
      map.put(split[0], split[1]);
    }

    return map;
  }

  private static boolean isMapFlag(String arg) {
    return isJdkMapFlag(arg) || isServerMapFlag(arg);
  }

  private static boolean isJdkMapFlag(String arg) {
    return JDK_RUNTIMES_MAPPINGS.equals(arg);
  }

  private static boolean isServerMapFlag(String arg) {
    return SERVER_RUNTIMES_MAPPINGS.equals(arg);
  }

  private static void printUsage() {
    System.out.println("\tUsage: BUILDER_JAR\n"
        + "\t\t--jdk-runtimes-map JDK_CONFIG_KEY=DOCKER_IMAGE [,...]\n"
        + "\t\t--server-runtimes-map SERVER_CONFIG_KEY=DOCKER_IMAGE [,...]");
  }
}
