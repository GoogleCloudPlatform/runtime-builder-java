package com.google.cloud.runtimes.builder.buildsteps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.runtimes.builder.TestUtils.TestWorkspaceBuilder;
import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.config.domain.AppYaml;
import com.google.cloud.runtimes.builder.config.domain.BetaSettings;
import com.google.cloud.runtimes.builder.config.domain.BuildContext;
import com.google.cloud.runtimes.builder.config.domain.JdkServerLookup;
import com.google.cloud.runtimes.builder.config.domain.RuntimeConfig;
import com.google.cloud.runtimes.builder.exception.ArtifactNotFoundException;
import com.google.cloud.runtimes.builder.exception.TooManyArtifactsException;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link PrebuiltRuntimeImageBuildStep}.
 */
public class PrebuiltRuntimeImageBuildStepTest {

  private PrebuiltRuntimeImageBuildStep prebuiltRuntimeImageBuildStep;

  @Mock private JdkServerLookup jdkServerLookup;
  private String compatImageName;
  private String legacyCompatImageName;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    compatImageName = "test-compat-image";
    prebuiltRuntimeImageBuildStep
        = new PrebuiltRuntimeImageBuildStep(jdkServerLookup, compatImageName,
        legacyCompatImageName);
  }

  @Test
  public void testProvidedJarArtifactPath() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder().build();

    String configuredArtifactPath = "artifactDir/my_artifact.jar";
    RuntimeConfig runtimeConfig = new RuntimeConfig();
    runtimeConfig.setArtifact(configuredArtifactPath);
    AppYaml appYaml = new AppYaml();
    appYaml.setRuntimeConfig(runtimeConfig);
    BuildContext buildContext = new BuildContext(appYaml, workspace, false);

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

    BuildContext buildContext = new BuildContext(new AppYaml(), workspace, false);
    prebuiltRuntimeImageBuildStep.run(buildContext);
  }

  @Test(expected = TooManyArtifactsException.class)
  public void testMultipleArtifactsWithCompat() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.jar").build()
        .file("foo.war/WEB-INF/web.xml").build()
        .build();

    BuildContext buildContext = new BuildContext(new AppYaml(), workspace, false);
    prebuiltRuntimeImageBuildStep.run(buildContext);
  }

  @Test
  public void testSingleWarArtifact() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.war").build()
        .build();

    BuildContext buildContext = new BuildContext(new AppYaml(), workspace, false);
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
    AppYaml appYaml = new AppYaml();
    appYaml.setRuntimeConfig(runtimeConfig);
    BuildContext buildContext = new BuildContext(appYaml, workspace, false);

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
    AppYaml appYaml = new AppYaml();
    appYaml.setRuntimeConfig(runtimeConfig);
    BuildContext buildContext = new BuildContext(appYaml, workspace, false);

    assertEquals(workspace.resolve("foo.jar"), prebuiltRuntimeImageBuildStep.getArtifact(buildContext).getPath());

    prebuiltRuntimeImageBuildStep.run(buildContext);
  }

  @Test(expected = ArtifactNotFoundException.class)
  public void testNoArtifacts() throws IOException, BuildStepException {
    Path workspace = new TestWorkspaceBuilder().build();
    BuildContext buildContext = new BuildContext(new AppYaml(), workspace, false);
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
        .file("WEB-INF/web.xml").build()
        .build();
    BuildContext buildContext = new BuildContext(new AppYaml(), workspace, false);
    prebuiltRuntimeImageBuildStep.run(buildContext);

    String dockerfile = buildContext.getDockerfile().toString();
    assertTrue(dockerfile.startsWith("FROM " + compatImageName + "\n"));
    assertTrue(dockerfile.contains("COPY ./ /app/"));
  }

  @Test
  public void testExplodedWarArtifactAtRoot() throws IOException, BuildStepException {
    String serverRuntime = "server_runtime_image";
    when(jdkServerLookup.lookupServerImage(null, null)).thenReturn(serverRuntime);
    Path workspace = new TestWorkspaceBuilder()
        .file("WEB-INF/web.xml").build()
        .build();
    BuildContext buildContext = new BuildContext(new AppYaml(), workspace, false);

    prebuiltRuntimeImageBuildStep.run(buildContext);

    String dockerfile = buildContext.getDockerfile().toString();
    assertTrue(dockerfile.startsWith("FROM " + compatImageName + "\n"));
    assertTrue(dockerfile.contains("COPY ./ /app/"));
  }

  @Test
  public void testCompatArtifactNotAtRoot() throws IOException, BuildStepException {
    String serverRuntime = "server_runtime_image";
    when(jdkServerLookup.lookupServerImage(null, null)).thenReturn(serverRuntime);
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.war/WEB-INF/web.xml").build()
        .build();
    BuildContext buildContext = new BuildContext(new AppYaml(), workspace, false);
    prebuiltRuntimeImageBuildStep.run(buildContext);
    String dockerfile = buildContext.getDockerfile().toString();

    assertTrue(dockerfile.startsWith("FROM " + compatImageName + "\n"));
    assertTrue(dockerfile.contains("COPY ./foo.war /app/"));
  }

  @Test(expected = BuildStepException.class)
  public void testForceCompatRuntimeWithWar() throws BuildStepException, IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("foo.war").build()
        .build();

    BetaSettings betaSettings = new BetaSettings();
    betaSettings.setEnableAppEngineApis(true);
    AppYaml appYaml = new AppYaml();
    appYaml.setBetaSettings(betaSettings);
    BuildContext buildContext = new BuildContext(appYaml, workspace, false);

    prebuiltRuntimeImageBuildStep.run(buildContext);
  }

  @Test
  public void testForceCompatRuntimeWithExplodedWar() throws BuildStepException, IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("WEB-INF/web.xml").build()
        .build();

    BetaSettings betaSettings = new BetaSettings();
    betaSettings.setEnableAppEngineApis(true);
    AppYaml appYaml = new AppYaml();
    appYaml.setBetaSettings(betaSettings);
    BuildContext buildContext = new BuildContext(appYaml, workspace, false);

    prebuiltRuntimeImageBuildStep.run(buildContext);

    String dockerfile = buildContext.getDockerfile().toString();
    assertTrue(dockerfile.startsWith("FROM " + compatImageName));
    assertTrue(dockerfile.contains("COPY ./ /app/"));
  }

  @Test
  public void testForceCompatRuntime() throws BuildStepException, IOException {
    Path workspace = new TestWorkspaceBuilder()
        .file("WEB-INF/web.xml").build()
        .file("WEB-INF/appengine-web.xml").build()
        .build();

    BetaSettings betaSettings = new BetaSettings();
    betaSettings.setEnableAppEngineApis(true);
    AppYaml appYaml = new AppYaml();
    appYaml.setBetaSettings(betaSettings);
    BuildContext buildContext = new BuildContext(appYaml, workspace, false);

    prebuiltRuntimeImageBuildStep.run(buildContext);

    String dockerfile = buildContext.getDockerfile().toString();
    assertTrue(dockerfile.startsWith("FROM " + compatImageName));
    assertTrue(dockerfile.contains("COPY ./ /app/"));
  }
}
