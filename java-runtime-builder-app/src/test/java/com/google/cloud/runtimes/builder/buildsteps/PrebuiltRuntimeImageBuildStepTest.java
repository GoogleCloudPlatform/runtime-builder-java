package com.google.cloud.runtimes.builder.buildsteps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.exception.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.exception.TooManyArtifactsException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Unit tests for {@link PrebuiltRuntimeImageBuildStep}.
 */
public class PrebuiltRuntimeImageBuildStepTest {

  private PrebuiltRuntimeImageBuildStep prebuiltRuntimeImageBuildStep;

  @Mock private JdkServerLookup jdkServerLookup;
  private String compatImageName;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    compatImageName = "test-compat-image";
    prebuiltRuntimeImageBuildStep
        = new PrebuiltRuntimeImageBuildStep(jdkServerLookup, compatImageName);
  }

  @Test
  public void testProvidedJarArtifactPath() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder().build();

    String configuredArtifactPath = "artifactDir/my_artifact.jar";
    RuntimeConfig runtimeConfig = new RuntimeConfig();
    runtimeConfig.setArtifact(configuredArtifactPath);
    BuildContext buildContext = new BuildContext(runtimeConfig, workspace);

    String image = "test_image";
    when(jdkServerLookup.lookupJdkImage(null)).thenReturn(image);

    prebuiltRuntimeImageBuildStep.run(buildContext);

    String expected = "FROM test_image\n"
        + "COPY ./artifactDir/my_artifact.jar $APP_DESTINATION\n";
    assertEquals(expected, buildContext.getDockerfile().toString());
  }

  @Test(expected = TooManyArtifactsException.class)
  public void testMultipleArtifacts() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.war").build()
        .file("bar.war").build()
        .build();

    BuildContext buildContext = new BuildContext(new RuntimeConfig(), workspace);
    prebuiltRuntimeImageBuildStep.run(buildContext);
  }

  @Test(expected = TooManyArtifactsException.class)
  public void testMultipleArtifactsWithCompat() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.jar").build()
        .file("foo.war/WEB-INF/web.xml").build()
        .build();

    BuildContext buildContext = new BuildContext(new RuntimeConfig(), workspace);
    prebuiltRuntimeImageBuildStep.run(buildContext);
  }

  @Test
  public void testSingleWarArtifact() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.war").build()
        .build();

    BuildContext buildContext = new BuildContext(new RuntimeConfig(), workspace);
    String image = "test_war_image";
    when(jdkServerLookup.lookupServerImage(null, null)).thenReturn(image);

    prebuiltRuntimeImageBuildStep.run(buildContext);
    String expected = "FROM test_war_image\n"
        + "COPY ./foo.war $APP_DESTINATION\n";
    assertEquals(expected, buildContext.getDockerfile().toString());
  }

  @Test
  public void testSingleJarArtifactWithCustomJdkOption() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.jar").build()
        .build();

    RuntimeConfig runtimeConfig = new RuntimeConfig();
    runtimeConfig.setJdk("custom_jdk");
    BuildContext buildContext = new BuildContext(runtimeConfig, workspace);

    String image = "custom_jdk_image";
    when(jdkServerLookup.lookupJdkImage("custom_jdk")).thenReturn(image);

    prebuiltRuntimeImageBuildStep.run(buildContext);
    String expected = "FROM custom_jdk_image\n"
        + "COPY ./foo.jar $APP_DESTINATION\n";
    assertEquals(expected, buildContext.getDockerfile().toString());
  }

  @Test(expected = BuildStepException.class)
  public void testSingleJarArtifactWithCustomJdkAndServerOption() throws IOException,
      BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.jar").build()
        .build();

    RuntimeConfig runtimeConfig = new RuntimeConfig();
    runtimeConfig.setJdk("custom_jdk");
    runtimeConfig.setServer("custom_server");
    BuildContext buildContext = new BuildContext(runtimeConfig, workspace);

    assertEquals(workspace.resolve("foo.jar"), prebuiltRuntimeImageBuildStep.getArtifact(buildContext));

    prebuiltRuntimeImageBuildStep.run(buildContext);
  }

  @Test(expected = ArtifactNotFoundException.class)
  public void testNoArtifacts() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder().build();
    BuildContext buildContext = new BuildContext(new RuntimeConfig(), workspace);
    prebuiltRuntimeImageBuildStep.run(buildContext);
  }

  @Test(expected = ArtifactNotFoundException.class)
  public void testUnrecognizedArtifact() throws IOException, BuildStepException {
    String artifact = "foo.xyz";
    Path workspace = new TestWorkspaceBuilder()
        .file(artifact).build()
        .build();
    BuildContext mockContext = mock(BuildContext.class);
    when(mockContext.getRuntimeConfig()).thenReturn(new RuntimeConfig());
    when(mockContext.getWorkspaceDir()).thenReturn(workspace);

    assertEquals(artifact, prebuiltRuntimeImageBuildStep.getArtifact(mockContext));

    // should throw an exception
    prebuiltRuntimeImageBuildStep.run(mockContext);
  }

  @Test
  public void testCompatArtifact() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("WEB-INF/appengine-web.xml").build()
        .build();
    BuildContext buildContext = new BuildContext(new RuntimeConfig(), workspace);
    prebuiltRuntimeImageBuildStep.run(buildContext);

    String dockerfile = buildContext.getDockerfile().toString();
    assertTrue(dockerfile.startsWith("FROM " + compatImageName + "\n"));
    assertTrue(dockerfile.contains("COPY ./ $APP_DESTINATION"));
  }

  @Test(expected = ArtifactNotFoundException.class)
  public void testNonCompatExplodedWarArtifactAtRoot() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("WEB-INF/web.xml").build()
        .build();
    BuildContext buildContext = new BuildContext(new RuntimeConfig(), workspace);
    prebuiltRuntimeImageBuildStep.run(buildContext);
  }

  @Test
  public void testCompatArtifactNotAtRoot() throws IOException, BuildStepException {
    String serverRuntime = "server_runtime_image";
    when(jdkServerLookup.lookupServerImage(null, null)).thenReturn(serverRuntime);
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.war/WEB-INF/web.xml").build()
        .build();
    BuildContext buildContext = new BuildContext(new RuntimeConfig(), workspace);
    prebuiltRuntimeImageBuildStep.run(buildContext);
    String dockerfile = buildContext.getDockerfile().toString();

    assertTrue(dockerfile.startsWith("FROM " + serverRuntime));
    assertTrue(dockerfile.contains("COPY " + "./foo.war $APP_DESTINATION"));
  }
}
