package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.injection.RootModule;
import com.google.cloud.runtimes.builder.workspace.TooManyArtifactsException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Application {

  private static final String EXECUTABLE_NAME = "java-builder";

  private final RuntimeBuilder runtimeBuilder;

  public Application(Path workspaceDir, Path appYaml) {
    Injector injector = Guice.createInjector(new RootModule(workspaceDir, appYaml));
    this.runtimeBuilder = injector.getInstance(RuntimeBuilder.class);
  }

  public void start() {
    try {
      runtimeBuilder.run();
    } catch (IOException  e) {
      // TODO handle
      throw new RuntimeException(e);
    }
  }

  /**
   * Main method for invocation from the command-line. Handles parsing of command-line options.
   */
  public static void main(String[] args) throws ParseException {
    Options options = new Options();
    options.addOption("c", "config", true, "absolute path to app.yaml config file");
    options.addOption("w", "workspace", true, "absolute path to workspace directory");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (!cmd.hasOption("w")) {
      // Print usage instructions and exit.
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(EXECUTABLE_NAME, options, true);
      System.exit(1);
    } else {
      Path workspace = Paths.get(cmd.getOptionValue("w"));
      Path appYaml = null;
      String appYamlValue = cmd.getOptionValue("c");
      if (appYamlValue != null) {
        appYaml = Paths.get(appYamlValue);
      }

      System.out.println("Starting run with workspace: " + workspace.toString());

      // Start the application.
      new Application(workspace, appYaml).start();
    }
  }
}
