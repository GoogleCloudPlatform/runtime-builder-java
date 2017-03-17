/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Module class for configuring Guice bindings.
 */
public class RootModule extends AbstractModule {

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
  @RuntimeDigests
  Properties provideRuntimeDigests() {
    InputStream runtimeDigestsStream = DefaultDockerfileGenerator.class.getClassLoader()
        .getResourceAsStream("runtimes.properties");
    if (runtimeDigestsStream == null) {
      throw new IllegalStateException("runtimes.properties file not found on classpath");
    }

    Properties properties = new Properties();
    try {
      properties.load(runtimeDigestsStream);
    } catch (IOException e) {
      // we want the process to fail fast if this happens
      throw new RuntimeException(e);
    }
    return properties;
  }

}
