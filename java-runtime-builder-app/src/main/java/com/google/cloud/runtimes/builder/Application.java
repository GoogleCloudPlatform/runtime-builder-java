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
import com.google.cloud.runtimes.builder.config.domain.BetaSettings;
import com.google.cloud.runtimes.builder.config.domain.OverrideableSetting;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.injection.RootModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
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
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Top-level class for executing from the command line.
 */
public class Application {

  private static final Options CLI_OPTIONS = new Options();
  private static final String EXECUTABLE_NAME = "<BUILDER>";

  public static final ImmutableMap<String, String> DEFAULT_JDK_MAPPINGS = ImmutableMap.of(
      "*", "gcr.io/google-appengine/openjdk:8",
      "openjdk8", "gcr.io/google-appengine/openjdk:8",
      "openjdk9", "gcr.io/google-appengine/openjdk:9"

  );
  public static final ImmutableMap<String, String> DEFAULT_SERVER_MAPPINGS;

  static {

    Map<String, String> serverSettings = new HashMap<>();
    serverSettings.put("*|*", "gcr.io/google-appengine/jetty:9");
    serverSettings.put("openjdk8|*", "gcr.io/google-appengine/jetty:9");
    serverSettings.put("openjdk8|jetty9", "gcr.io/google-appengine/jetty:9");
    serverSettings.put("openjdk8|jetty", "gcr.io/google-appengine/jetty:9");
    serverSettings.put("openjdk8|tomcat8", "gcr.io/google-appengine/tomcat:8");
    serverSettings.put("openjdk8|tomcat", "gcr.io/google-appengine/tomcat:8");
    serverSettings.put("*|jetty9", "gcr.io/google-appengine/jetty:9");
    serverSettings.put("*|jetty", "gcr.io/google-appengine/jetty:latest");
    serverSettings.put("*|tomcat8", "gcr.io/google-appengine/tomcat:8");
    serverSettings.put("*|tomcat", "gcr.io/google-appengine/tomcat:latest");

    DEFAULT_SERVER_MAPPINGS = ImmutableMap.copyOf(serverSettings);

    CLI_OPTIONS.addOption(Option.builder("j")
        .hasArgs()
        .longOpt("jdk-runtimes-map")
        .desc("Mappings between supported jdk versions and docker images")
        .build());

    CLI_OPTIONS.addOption(Option.builder("s")
        .hasArgs()
        .longOpt("server-runtimes-map")
        .desc("Mappings between supported jdk versions, server types, and docker images")
        .build());

    CLI_OPTIONS.addOption(Option.builder("c")
        .required()
        .hasArgs()
        .longOpt("compat-runtime-image")
        .desc("Base runtime image to use for the flex-compat environment")
        .build());

    CLI_OPTIONS.addOption(Option.builder("m")
        .required()
        .hasArg()
        .longOpt("maven-docker-image")
        .desc("Docker image to use for maven builds")
        .build());

    CLI_OPTIONS.addOption(Option.builder("g")
        .required()
        .hasArg()
        .longOpt("gradle-docker-image")
        .desc("Docker image to use for gradle builds")
        .build());

    CLI_OPTIONS.addOption(Option.builder("n")
        .hasArg(false)
        .longOpt("no-source-build")
        .desc("Disable building from source")
        .build());

    addOverrideSettingsToOptions(CLI_OPTIONS);
  }

  /**
   * Main method for invocation from the command line. Handles parsing of command line options.
   */
  public static void main(String[] args) throws BuildStepException, IOException {
    CommandLine cmd = parse(args);
    String[] jdkMappings = cmd.getOptionValues("j");
    String[] serverMappings = cmd.getOptionValues("s");
    String compatImage = cmd.getOptionValue("c");
    String mavenImage = cmd.getOptionValue("m");
    String gradleImage = cmd.getOptionValue("g");
    boolean disableSourceBuild = cmd.hasOption("n");

    Injector injector = Guice.createInjector(
        new RootModule(jdkMappings, DEFAULT_JDK_MAPPINGS, serverMappings, DEFAULT_SERVER_MAPPINGS,
            compatImage, mavenImage, gradleImage,
            disableSourceBuild, getAppYamlOverrideSettings(cmd)));

    // Perform dependency injection and run the application
    Path workspaceDir  = Paths.get(System.getProperty("user.dir"));
    injector.getInstance(BuildPipelineConfigurator.class).generateDockerResources(workspaceDir);
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

  /**
   * Gets the override settings from the command line.
   *
   * @param cmd the command line in which to check for the settings.
   * @return a map of the settings.
   */
  @VisibleForTesting
  public static Map<String, Object> getAppYamlOverrideSettings(CommandLine cmd) {
    List<Field> configFields = OverrideableSetting.getOverridableFields(RuntimeConfig.class);
    configFields.addAll(OverrideableSetting.getOverridableFields(BetaSettings.class));
    Map<String, Object> configMap = new HashMap<>();
    doForEachOverrideSetting(configFields,
        name -> {
          if (cmd.hasOption(name)) {
            configMap.put(name, true);
          }
        },
        name -> {
          String value = cmd.getOptionValue(name);
          if (value != null) {
            configMap.put(name, cmd.getOptionValue(name));
          }
        }
    );
    return configMap;
  }

  /**
   * Adds options for override settings to app.yaml to command line options.
   *
   * @param options the command line options to which to add these settings.
   */
  @VisibleForTesting
  public static void addOverrideSettingsToOptions(Options options) {
    String overrideSettingDescPrefix = "Replaces the setting from app.yaml under";

    doForEachOverrideSetting(OverrideableSetting.getOverridableFields(RuntimeConfig.class),
        booleanSettingName -> {
          options.addOption(Option.builder().longOpt(booleanSettingName)
              .desc(overrideSettingDescPrefix + " runtime_config : " + booleanSettingName)
              .hasArg(false)
              .build());
        },
        nonBooleanSettingName -> {
          options.addOption(Option.builder().longOpt(nonBooleanSettingName)
              .desc(overrideSettingDescPrefix + " runtime_config : " + nonBooleanSettingName)
              .build());
        }
    );

    doForEachOverrideSetting(OverrideableSetting.getOverridableFields(BetaSettings.class),
        booleanSettingName -> {
          options.addOption(Option.builder().longOpt(booleanSettingName)
              .desc(overrideSettingDescPrefix + " beta_settings : " + booleanSettingName)
              .hasArg(false)
              .build());
        },
        nonBooleanSettingName -> {
          options.addOption(Option.builder().longOpt(nonBooleanSettingName)
              .desc(overrideSettingDescPrefix + " beta_settings : " + nonBooleanSettingName)
              .build());
        }
    );
  }

  private static void doForEachOverrideSetting(List<Field> configFields,
      Consumer<String> actionBooleanSetting, Consumer<String> actionStringSetting) {
    for (Field field : configFields) {
      String name = OverrideableSetting.getSettingName(field);
      if ((field.getType().equals(boolean.class) || field.getType().equals(Boolean.class))) {
        actionBooleanSetting.accept(name);
      } else {
        actionStringSetting.accept(name);
      }
    }
  }
}
