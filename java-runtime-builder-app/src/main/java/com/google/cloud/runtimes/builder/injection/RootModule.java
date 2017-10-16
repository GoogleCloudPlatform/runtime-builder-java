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

package com.google.cloud.runtimes.builder.injection;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepFactory;
import com.google.cloud.runtimes.builder.config.AppYamlFinder;
import com.google.cloud.runtimes.builder.config.AppYamlParser;
import com.google.cloud.runtimes.builder.config.YamlParser;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.BuildContextFactory;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Module class for configuring Guice bindings.
 */
public class RootModule extends AbstractModule {

  private final String[] jdkMappings;
  private final String[] serverMappings;
  private final ImmutableMap<String, String> defaultJdkMappings;
  private final ImmutableMap<String, String> defaultServerMappings;
  private final String compatImage;
  private final String mavenDockerImage;
  private final String gradleDockerImage;
  private final boolean disableSourceBuild;

  private final Map<String, Object> commandLineOverrideSettings;

  private static final String CONFIG_YAML_ENV_VAR = "GAE_APPLICATION_YAML_PATH";

  /**
   * Constructs a new {@link RootModule} for Guice.
   *
   * @param jdkMappings mappings between supported jdk versions and docker images
   * @param defaultJdkMappings a default map of settings for jdk versions and docker images
   * @param serverMappings mappings between supported jdk versions, server types, and docker images
   * @param defaultServerMappings a default map of supported jdk versions, server types, and images
   * @param compatImage compat runtime docker image
   * @param mavenDockerImage maven builder docker image
   * @param gradleDockerImage gradle builder docker image
   * @param disableSourceBuild disables the building of images from source
   * @param commandLineOverrideSettings a map of settings from commandline to override
   */
  public RootModule(String[] jdkMappings, ImmutableMap<String, String> defaultJdkMappings,
      String[] serverMappings,
      ImmutableMap<String, String> defaultServerMappings, String compatImage,
      String mavenDockerImage, String gradleDockerImage, boolean disableSourceBuild,
      Map<String, Object> commandLineOverrideSettings) {
    Preconditions.checkNotNull(defaultJdkMappings);
    Preconditions.checkNotNull(defaultServerMappings);
    Preconditions.checkNotNull(compatImage);
    Preconditions.checkNotNull(mavenDockerImage);
    Preconditions.checkNotNull(gradleDockerImage);
    Preconditions.checkNotNull(commandLineOverrideSettings);

    this.jdkMappings = jdkMappings;
    this.serverMappings = serverMappings;
    this.defaultJdkMappings = defaultJdkMappings;
    this.defaultServerMappings = defaultServerMappings;
    this.compatImage = compatImage;
    this.mavenDockerImage = mavenDockerImage;
    this.gradleDockerImage = gradleDockerImage;
    this.disableSourceBuild = disableSourceBuild;

    this.commandLineOverrideSettings = commandLineOverrideSettings;
  }

  /**
   * Constructs a new {@link RootModule} for Guice.
   *
   * @param jdkMappings mappings between supported jdk versions and docker images
   * @param serverMappings mappings between supported jdk versions, server types, and docker images
   * @param compatImage compat runtime docker image
   * @param mavenDockerImage maven builder docker image
   * @param gradleDockerImage gradle builder docker image
   * @param disableSourceBuild disables the building of images from source
   * @param commandLineOverrideSettings a map of settings from commandline to override
   */
  public RootModule(String[] jdkMappings, String[] serverMappings,
      String compatImage,
      String mavenDockerImage, String gradleDockerImage, boolean disableSourceBuild,
      Map<String, Object> commandLineOverrideSettings) {
    this(jdkMappings, ImmutableMap.of(), serverMappings, ImmutableMap.of(), compatImage,
        mavenDockerImage,
        gradleDockerImage, disableSourceBuild, commandLineOverrideSettings);
  }

  /**
   * Constructs a new {@link RootModule} for Guice.
   *
   * @param jdkMappings mappings between supported jdk versions and docker images
   * @param serverMappings mappings between supported jdk versions, server types, and docker images
   * @param compatImage compat runtime docker image
   * @param mavenDockerImage maven builder docker image
   * @param gradleDockerImage gradle builder docker image
   * @param disableSourceBuild disables the building of images from source
   */
  public RootModule(String[] jdkMappings, String[] serverMappings, String compatImage,
      String mavenDockerImage, String gradleDockerImage, boolean disableSourceBuild) {
    this(jdkMappings, serverMappings, compatImage,
        mavenDockerImage, gradleDockerImage,
        disableSourceBuild,
        Collections.emptyMap());
  }

  @Override
  protected void configure() {
    bind(new TypeLiteral<Optional<String>>(){})
        .annotatedWith(ConfigYamlPath.class)
        .toInstance(Optional.ofNullable(System.getenv(CONFIG_YAML_ENV_VAR)));

    bind(new TypeLiteral<Map<String, Object>>() {
    })
        .annotatedWith(CommandLineOverrideSettings.class)
        .toInstance(commandLineOverrideSettings);

    bind(String.class)
        .annotatedWith(CompatDockerImage.class)
        .toInstance(compatImage);
    bind(String.class)
        .annotatedWith(MavenDockerImage.class)
        .toInstance(mavenDockerImage);
    bind(String.class)
        .annotatedWith(GradleDockerImage.class)
        .toInstance(gradleDockerImage);
    bind(Boolean.class)
        .annotatedWith(DisableSourceBuild.class)
        .toInstance(disableSourceBuild);

    bind(new TypeLiteral<YamlParser<AppYaml>>(){})
        .to(AppYamlParser.class);
    bind(AppYamlFinder.class);

    install(new FactoryModuleBuilder()
        .build(BuildStepFactory.class));
    install(new FactoryModuleBuilder()
        .build(BuildContextFactory.class));
  }

  /*
   * Converts an array of mapping strings, with keys and values separated by an '=' character, into
   * a Map<String,String>.
   */
  private static Map<String, String> buildMap(String[] mappings,
      ImmutableMap<String, String> defaultMappings) {
    Map<String, String> map = new HashMap<>(defaultMappings);
    if (mappings == null) {
      return map;
    }
    map.putAll(Arrays.stream(mappings)
        .map(s -> {
          String[] split = s.split("=");
          // make sure mappings are formatted correctly
          if (split.length != 2)  {
            throw new IllegalArgumentException("Invalid mapping: '" + s + "'. "
                + "All jdk/server mappings must be formatted as: KEY=VAL");
          }
          return split;
        })
        .collect(Collectors.toMap(a -> a[0], a -> a[1])));
    return map;
  }

  @Provides
  protected JdkServerLookup provideJdkServerLookup() throws IOException {
    return new JdkServerLookup(buildMap(jdkMappings, defaultJdkMappings),
        buildMap(serverMappings, defaultServerMappings));
  }
}
