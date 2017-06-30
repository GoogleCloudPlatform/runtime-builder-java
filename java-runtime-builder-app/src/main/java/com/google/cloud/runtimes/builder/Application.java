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
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Top-level class for executing from the command line.
 */
public class Application {

  private static final Options CLI_OPTIONS = new Options();
  private static final String EXECUTABLE_NAME = "<BUILDER_JAR>";

  static {
    CLI_OPTIONS.addOption(Option.builder("j")
        .required()
        .hasArgs()
        .longOpt("jdk-runtimes-map")
        .desc("Mappings between supported jdk versions and docker images")
        .build());

    CLI_OPTIONS.addOption(Option.builder("s")
        .required()
        .hasArgs()
        .longOpt("server-runtimes-map")
        .desc("Mappings between supported jdk versions, server types, and docker images")
        .build());
  }

  /**
   * Main method for invocation from the command line. Handles parsing of command line options.
   */
  public static void main(String[] args)
      throws BuildStepException, IOException, AppYamlNotFoundException {
    CommandLine cmd = parse(args);
    String[] jdkMappings = cmd.getOptionValues("j");
    String[] serverMappings = cmd.getOptionValues("s");

    Path workspaceDir  = Paths.get(System.getProperty("user.dir"));
    build(jdkMappings, serverMappings, workspaceDir);
  }

  /**
   * Invokes the builder.
   */
  public static void build(String[] jdkMappings, String[] serverMappings, Path workspaceDir)
      throws BuildStepException, IOException, AppYamlNotFoundException {
    // Perform dependency injection and run the application
    Injector injector = Guice.createInjector(new RootModule(jdkMappings, serverMappings));
    injector.getInstance(BuildPipeline.class).build(workspaceDir);
  }

  private static CommandLine parse(String[] args) {
    CommandLineParser parser = new DefaultParser();
    try {
      return parser.parse(CLI_OPTIONS, args);
    } catch (ParseException e) {
      // print instructions and exit
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(EXECUTABLE_NAME, CLI_OPTIONS, true);
      System.exit(1);
    }
    return null;
  }
}
