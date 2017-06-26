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
import com.google.cloud.runtimes.builder.buildsteps.docker.DefaultDockerfileGenerator;
import com.google.cloud.runtimes.builder.buildsteps.docker.DockerfileGenerator;
import com.google.cloud.runtimes.builder.config.AppYamlParser;
import com.google.cloud.runtimes.builder.config.YamlParser;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * Module class for configuring Guice bindings.
 */
public class RootModule extends AbstractModule {

  private final String jdkMapArg;
  private final String serverMapArg;

  /**
   * Constructs a new {@link RootModule} for Guice.
   *
   * @param jdkMapArg a JSON object that maps jdk names to docker images
   * @param serverMapArg a JSON object that maps jdk and server names to docker images
   */
  public RootModule(String jdkMapArg, String serverMapArg) {
    this.jdkMapArg = jdkMapArg;
    this.serverMapArg = serverMapArg;
  }

  @Override
  protected void configure() {
    bind(new TypeLiteral<YamlParser<AppYaml>>(){})
        .to(AppYamlParser.class);
    bind(DockerfileGenerator.class)
        .to(DefaultDockerfileGenerator.class);

    install(new FactoryModuleBuilder()
        .build(BuildStepFactory.class));
  }

  @Provides
  protected JdkServerLookup provideJdkServerLookup() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> jdkMap  = objectMapper.readValue(jdkMapArg,
        new TypeReference<Map<String, String>>(){});
    Map<String, String> serverMap  = objectMapper.readValue(serverMapArg,
        new TypeReference<Map<String, String>>(){});

    return new JdkServerLookup(jdkMap, serverMap);
  }

}
