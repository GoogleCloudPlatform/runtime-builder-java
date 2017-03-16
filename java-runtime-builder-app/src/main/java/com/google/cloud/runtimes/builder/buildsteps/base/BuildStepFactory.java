package com.google.cloud.runtimes.builder.buildsteps.base;

import com.google.cloud.runtimes.builder.buildsteps.docker.StageDockerArtifactBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.gradle.GradleBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.maven.MavenBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.script.ScriptExecutionBuildStep;

import java.util.Optional;

/**
 * Factory interface to simplify instantiation of objects with Guice-provided dependencies. See
 * <a href="https://github.com/google/guice/wiki/AssistedInject#assistedinject-in-guice-30">docs</a>
 *  for more.
 */
public interface BuildStepFactory {

  MavenBuildStep createMavenBuildStep();

  GradleBuildStep createGradleBuildStep();

  StageDockerArtifactBuildStep createStageDockerArtifactBuildStep(Optional<String> artifactPath);

  ScriptExecutionBuildStep createScriptExecutionBuildStep(String buildCommand);

}
