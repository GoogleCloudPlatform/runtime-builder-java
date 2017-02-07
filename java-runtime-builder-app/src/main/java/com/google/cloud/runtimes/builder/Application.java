package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.injection.RootModule;
import com.google.cloud.runtimes.builder.exception.RuntimeBuilderException;
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

  private static final Options CLI_OPTIONS = new Options();
  private static final String EXECUTABLE_NAME = "<BUILDER_JAR>";
  private final RuntimeBuilder runtimeBuilder;

  static {
    CLI_OPTIONS.addOption("c", "config", true, "absolute path to app.yaml config file");
    CLI_OPTIONS.addOption("w", "workspace", true, "absolute path to workspace directory");
  }

  public Application(Path workspaceDir, Path appYaml) {
    Injector injector = Guice.createInjector(new RootModule(workspaceDir, appYaml));
    this.runtimeBuilder = injector.getInstance(RuntimeBuilder.class);
  }

  public void start() {
    try {
      runtimeBuilder.run();
    } catch (IOException | RuntimeBuilderException e) {
      // TODO log? make sure exceptions will get logged
      throw new RuntimeException(e);
    }
  }

  /**
   * Main method for invocation from the command-line. Handles parsing of command-line options.
   */
  public static void main(String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(CLI_OPTIONS, args);
    } catch (ParseException e) {
      printInstructionsAndExit(1);
    }

    if (!cmd.hasOption("w")) {
      printInstructionsAndExit(1);
    } else {
      Path workspace = Paths.get(cmd.getOptionValue("w"));
      Path appYaml = null;
      String appYamlValue = cmd.getOptionValue("c");
      if (appYamlValue != null) {
        appYaml = Paths.get(appYamlValue);
      }

      // Start the application.
      new Application(workspace, appYaml).start();
    }
  }

  private static void printInstructionsAndExit(int statusCode) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(EXECUTABLE_NAME, CLI_OPTIONS, true);
    System.exit(statusCode);
  }
}
