package com.google.cloud.runtimes.builder.buildsteps;

import static junit.framework.TestCase.assertEquals;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

import org.junit.Test;

import java.io.IOException;

/**
 * Unit tests for {@link JettyOptionsBuildStep}.
 */
public class JettyOptionsBuildStepTest {

  @Test
  public void testNoJettyQuickstart() throws IOException, BuildStepException {
    BuildContext ctx = initBuildContext(new RuntimeConfig());

    String dockerfileBefore = ctx.getDockerfile().toString();

    JettyOptionsBuildStep buildStep = new JettyOptionsBuildStep();
    buildStep.run(ctx);

    assertEquals(dockerfileBefore, ctx.getDockerfile().toString());
  }

  @Test
  public void testWithJettyQuickstart() throws IOException, BuildStepException {
    RuntimeConfig runtimeConfig = new RuntimeConfig();
    runtimeConfig.setJettyQuickstart(true);
    BuildContext ctx = initBuildContext(runtimeConfig);

    String dockerfileBefore = ctx.getDockerfile().toString();

    JettyOptionsBuildStep buildStep = new JettyOptionsBuildStep();
    buildStep.run(ctx);

    String expected = dockerfileBefore + JettyOptionsBuildStep.JETTY_QUICKSTART_COMMAND + "\n";
    assertEquals(expected, ctx.getDockerfile().toString());
  }

  private BuildContext initBuildContext(RuntimeConfig runtimeConfig) throws IOException {
    return new BuildContext(runtimeConfig, new TestWorkspaceBuilder().build());
  }

}
