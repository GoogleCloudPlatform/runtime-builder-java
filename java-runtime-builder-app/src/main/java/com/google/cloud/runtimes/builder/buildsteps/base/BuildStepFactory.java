package com.google.cloud.runtimes.builder.buildsteps.base;

import com.google.cloud.runtimes.builder.buildsteps.docker.StageDockerArtifactBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.gradle.GradleBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.maven.MavenBuildStep;
import com.google.cloud.runtimes.builder.buildsteps.script.ScriptExecutionBuildStep;

import java.util.Optional;

public interface BuildStepFactory {

  MavenBuildStep createMavenBuildStep();

  GradleBuildStep createGradleBuildStep();

  StageDockerArtifactBuildStep createStageDockerArtifactBuildStep(Optional<String> artifactPath);

  ScriptExecutionBuildStep createScriptExecutionBuildStep(String buildCommand);

}
