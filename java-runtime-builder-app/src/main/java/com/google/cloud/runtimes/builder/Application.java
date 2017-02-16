package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
import com.google.cloud.runtimes.builder.exception.BuildStepException;
import com.google.cloud.runtimes.builder.exception.RuntimeBuilderException;
import com.google.cloud.runtimes.builder.injection.RootModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Top-level class for executing from the command line.
 */
public class Application {

  private static final Options CLI_OPTIONS = new Options();
  private static final String EXECUTABLE_NAME = "<BUILDER_JAR>";

  static {
    CLI_OPTIONS.addOption("c", "config", true, "absolute path to app.yaml config file");
    CLI_OPTIONS.addOption("w", "workspace", true, "absolute path to workspace directory");
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

      // Perform dependency injection and run the pipeline
      Injector injector = Guice.createInjector(new RootModule());
      try {
        injector.getInstance(BuildPipeline.class).build(workspace);
      } catch (IOException | AppYamlNotFoundException | BuildStepException e) {
        // TODO log? make sure exceptions will get logged
        throw new RuntimeException(e);
      }
    }
  }

  private static void printInstructionsAndExit(int statusCode) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(EXECUTABLE_NAME, CLI_OPTIONS, true);
    System.exit(statusCode);
  }
}
