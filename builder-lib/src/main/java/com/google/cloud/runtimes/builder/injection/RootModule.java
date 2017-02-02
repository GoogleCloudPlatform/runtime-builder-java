package com.google.cloud.runtimes.builder.injection;

import com.google.cloud.runtimes.builder.docker.DefaultDockerfileGenerator;
import com.google.cloud.runtimes.builder.docker.DockerfileGenerator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Module class for configuring Guice bindings.
 */
public class RootModule extends AbstractModule {

  private final Path workspaceDir;
  private final Path appYaml;

  public RootModule(Path workspaceDir, Path appYaml) {
    this.workspaceDir = workspaceDir;
    this.appYaml = appYaml;
  }

  @Override
  protected void configure() {
    bind(new TypeLiteral<Optional<Path>>(){})
        .annotatedWith(AppYamlPath.class)
        .toInstance(Optional.ofNullable(appYaml));

    bind(Path.class)
        .annotatedWith(WorkspacePath.class)
        .toInstance(workspaceDir);

    bind(DockerfileGenerator.class)
        .to(DefaultDockerfileGenerator.class);
  }

  @Provides
  Properties provideRuntimeDigestsProperties() {
    InputStream runtimeDigestsStream
        = DefaultDockerfileGenerator.class.getClassLoader().getResourceAsStream("runtimes.properties");
    if (runtimeDigestsStream == null) {
      throw new IllegalStateException("runtimes.properties file not found on classpath");
    }

    Properties properties = new Properties();
    try {
      properties.load(runtimeDigestsStream);
    } catch (IOException e) {
      // TODO
      e.printStackTrace();
    }
    return properties;
  }

}
