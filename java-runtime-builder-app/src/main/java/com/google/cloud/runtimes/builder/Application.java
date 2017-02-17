package com.google.cloud.runtimes.builder;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;
import com.google.cloud.runtimes.builder.exception.AppYamlNotFoundException;
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
    CLI_OPTIONS.addOption("w", "workspace", true, "absolute path to workspace directory");
  }

  /**
   * Main method for invocation from the command line. Handles parsing of command line options.
   */
  public static void main(String[] args) {
    Path workspace = parseWorkspaceOption(args);

    // Perform dependency injection and run the application
    Injector injector = Guice.createInjector(new RootModule());
    try {
      injector.getInstance(BuildPipeline.class).build(workspace);
    } catch (IOException | AppYamlNotFoundException | BuildStepException e) {
      throw new RuntimeException(e);
    }
  }

  private static Path parseWorkspaceOption(String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(CLI_OPTIONS, args);
    } catch (ParseException e) {
      // print instructions and exit
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(EXECUTABLE_NAME, CLI_OPTIONS, true);
      System.exit(1);
    }

    Path workspace;
    if (!cmd.hasOption("w")) {
      // use the current working directory as a default
      workspace = Paths.get(System.getProperty("user.dir"));
    } else {
      workspace = Paths.get(cmd.getOptionValue("w"));
    }
    return workspace;
  }
}
