package com.google.cloud.runtimes.builder.buildsteps;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link SourceBuildRuntimeImageBuildStep}.
 */
public class SourceBuildRuntimeImageBuildStepTest {

  @Mock JdkServerLookup jdkServerLookup;

  private SourceBuildRuntimeImageBuildStep buildStep;
  private RuntimeConfig runtimeConfig;

  private static final String TEST_JDK_RUNTIME = "test_jdk_runtime";
  private static final String TEST_SERVER_RUNTIME = "test_server_runtime";
  private static final String TEST_COMPAT_RUNTIME = "test_compat_runtime";

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    // init runtimeConfig with some common values
    runtimeConfig = new RuntimeConfig();
    runtimeConfig.setServer("server_arg");
    runtimeConfig.setJdk("jdk_arg");

    when(jdkServerLookup.lookupJdkImage(eq(runtimeConfig.getJdk()))).thenReturn(TEST_JDK_RUNTIME);
    when(jdkServerLookup.lookupServerImage(eq(runtimeConfig.getJdk()),
        eq(runtimeConfig.getServer()))).thenReturn(TEST_SERVER_RUNTIME);

    buildStep = new SourceBuildRuntimeImageBuildStep(jdkServerLookup, TEST_COMPAT_RUNTIME);
  }

  private BuildContext initBuildContext() throws IOException {
    return new BuildContext(runtimeConfig, new TestWorkspaceBuilder().build(), false);
  }

  @Test(expected = IllegalStateException.class)
  public void testNoArtifactSpecified() throws IOException, BuildStepException {
    BuildContext ctx = initBuildContext();
    buildStep.run(ctx);
  }

  @Test
  public void testWithArtifactSpecified() throws IOException, BuildStepException {
    String artifact = "path/to/artifact.war";
    BuildContext ctx = initBuildContext();
    ctx.getRuntimeConfig().setArtifact(artifact);
    buildStep.run(ctx);

    assertEquals("FROM " + TEST_SERVER_RUNTIME + "\nCOPY " + "./" + artifact + " $APP_DESTINATION\n",
        ctx.getDockerfile().toString());
  }

}
