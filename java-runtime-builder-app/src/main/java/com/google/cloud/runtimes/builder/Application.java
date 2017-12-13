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
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.config.domain.OverrideableSetting;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.injection.RootModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ObjectArrays;
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

  public static final String[] DEFAULT_JDK_MAPPINGS = {
      "*=gcr.io/google-appengine/openjdk:8",
      "openjdk8=gcr.io/google-appengine/openjdk:8",
      "openjdk9=gcr.io/google-appengine/openjdk:9"
  };
  public static final String[] DEFAULT_SERVER_MAPPINGS = {
      "*|*=gcr.io/google-appengine/jetty:9",
      "openjdk8|*=gcr.io/google-appengine/jetty:9",
      "openjdk8|jetty9=gcr.io/google-appengine/jetty:9",
      "openjdk8|jetty=gcr.io/google-appengine/jetty:9",
      "openjdk8|tomcat8=gcr.io/google-appengine/tomcat:8",
      "openjdk8|tomcat=gcr.io/google-appengine/tomcat:8",
      "*|jetty9=gcr.io/google-appengine/jetty:9",
      "*|jetty=gcr.io/google-appengine/jetty:latest",
      "*|tomcat8=gcr.io/google-appengine/tomcat:8",
      "*|tomcat=gcr.io/google-appengine/tomcat:latest"
  };
  public static final String DEFAULT_COMPAT_RUNTIME_IMAGE =
      "gcr.io/google-appengine/jetty9-compat:latest";
  public static final String DEFAULT_MAVEN_DOCKER_IMAGE =
      "gcr.io/cloud-builders/mvn:3.5.0-jdk-8";
  public static final String DEFAULT_GRADLE_DOCKER_IMAGE =
      "gcr.io/cloud-builders/gradle:4.0-jdk-8";
  private static final Options CLI_OPTIONS = new Options();
  private static final String EXECUTABLE_NAME = "<BUILDER>";

  /**
   * Adds the settings needed for the builder to an Options.
   *
   * @param options The options to which to add the required args.
   */
  @VisibleForTesting
  public static void addCliOptions(Options options) {

    options.addOption(Option.builder("j")
        .hasArgs()
        .longOpt("jdk-runtimes-map")
        .desc("Mappings between supported jdk versions and docker images")
        .build());

    options.addOption(Option.builder("s")
        .hasArgs()
        .longOpt("server-runtimes-map")
        .desc("Mappings between supported jdk versions, server types, and docker images")
        .build());

    options.addOption(Option.builder("c")
        .hasArgs()
        .longOpt("compat-runtime-image")
        .desc("Base runtime image to use for the flex-compat environment")
        .build());

    options.addOption(Option.builder("m")
        .hasArg()
        .longOpt("maven-docker-image")
        .desc("Docker image to use for maven builds")
        .build());

    options.addOption(Option.builder("g")
        .hasArg()
        .longOpt("gradle-docker-image")
        .desc("Docker image to use for gradle builds")
        .build());

    options.addOption(Option.builder("n")
        .hasArg(false)
        .longOpt("no-source-build")
        .desc("Disable building from source")
        .build());

    addOverrideSettingsToOptions(options);
  }

  /**
   * Main method for invocation from the command line. Handles parsing of command line options.
   */
  public static void main(String[] args) throws BuildStepException, IOException {
    addCliOptions(CLI_OPTIONS);
    CommandLine cmd = parse(args);
    String[] jdkMappings = cmd.getOptionValues("j");
    String[] serverMappings = cmd.getOptionValues("s");
    String compatImage = cmd.getOptionValue("c");
    String mavenImage = cmd.getOptionValue("m");
    String gradleImage = cmd.getOptionValue("g");
    boolean disableSourceBuild = cmd.hasOption("n");

    Injector injector = Guice.createInjector(
        new RootModule(mergeSettingsWithDefaults(jdkMappings, serverMappings),
            compatImage == null ? DEFAULT_COMPAT_RUNTIME_IMAGE : compatImage,
            mavenImage == null ? DEFAULT_MAVEN_DOCKER_IMAGE : mavenImage,
            gradleImage == null ? DEFAULT_GRADLE_DOCKER_IMAGE : gradleImage,
            disableSourceBuild, getAppYamlOverrideSettings(cmd)));

    // Perform dependency injection and run the application
    Path workspaceDir = Paths.get(System.getProperty("user.dir"));
    injector.getInstance(BuildPipelineConfigurator.class).generateDockerResources(workspaceDir);
  }

  private static CommandLine parse(String[] args) {
    CommandLineParser parser = new DefaultParser();
    try {
      return parser.parse(CLI_OPTIONS, args);
    } catch (ParseException e) {
      // print instructions and exit
      System.out.println(e.getMessage());
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
              .hasArg()
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
              .hasArg()
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

  /**
   * Merges the given raw commandline settings with default settings for jdk and server images.
   *
   * @param rawJdkSettings the raw commandling jdk mapping settings.
   * @param rawServerSettings the raw commandline server mapping settings.
   * @return the merged settings.
   */
  @VisibleForTesting
  public static JdkServerLookup mergeSettingsWithDefaults(String[] rawJdkSettings,
      String[] rawServerSettings) {
    String[] jdk = rawJdkSettings == null ? new String[0] : rawJdkSettings;
    String[] server = rawServerSettings == null ? new String[0] : rawServerSettings;
    return new JdkServerLookup(
        ObjectArrays.concat(jdk, DEFAULT_JDK_MAPPINGS, String.class),
        ObjectArrays.concat(server, DEFAULT_SERVER_MAPPINGS, String.class));
  }
}
