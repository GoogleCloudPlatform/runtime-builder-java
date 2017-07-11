package com.google.cloud.runtimes.builder.buildsteps;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Unit tests for {@link SourceBuildRuntimeImageBuildStep}.
 */
public class SourceBuildRuntimeImageBuildStepTest {

  @Mock JdkServerLookup jdkServerLookup;

  private SourceBuildRuntimeImageBuildStep buildStep;
  private RuntimeConfig runtimeConfig;

  private static final String TEST_JDK_RUNTIME = "test_jdk_runtime";
  private static final String TEST_SERVER_RUNTIME = "test_server_runtime";

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

    buildStep = new SourceBuildRuntimeImageBuildStep(jdkServerLookup);
  }

  private BuildContext initBuildContext() throws IOException {
    return new BuildContext(runtimeConfig, new TestWorkspaceBuilder().build());
  }

  @Test
  public void testServerAndJdk() throws IOException, BuildStepException {
    BuildContext ctx = initBuildContext();
    ctx.setBuildArtifactLocation(Optional.of(Paths.get("/workdir/foo")));

    buildStep.run(ctx);

    assertEquals("FROM " + TEST_SERVER_RUNTIME + "\nCOPY /workdir/foo/*.war $APP_DESTINATION\n",
        ctx.getDockerfile().toString());
  }

}
