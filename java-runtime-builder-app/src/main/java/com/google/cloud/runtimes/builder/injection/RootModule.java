package com.google.cloud.runtimes.builder.injection;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepFactory;
import com.google.cloud.runtimes.builder.buildsteps.docker.DefaultDockerfileGenerator;
import com.google.cloud.runtimes.builder.buildsteps.docker.DockerfileGenerator;
import com.google.cloud.runtimes.builder.buildsteps.docker.StageDockerArtifactBuildStep;
import com.google.cloud.runtimes.builder.config.AppYamlParser;
import com.google.cloud.runtimes.builder.config.YamlParser;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Module class for configuring Guice bindings.
 */
public class RootModule extends AbstractModule {

  private final Path workspacePath;

  public RootModule(Path workspacePath) {
    this.workspacePath = workspacePath;
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
