package com.google.cloud.runtimes.builder.buildsteps;

import com.google.cloud.runtimes.builder.exception.BuildStepException;
import com.google.common.base.Preconditions;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class BuildStep {

  private static final String METADATA_FILE_NAME = ".build_step_metadata";
  private Path metaDataPath;

  protected abstract void doBuild(Path directory, Map<String, String> metadata)
      throws BuildStepException;

  public void run(Path directory) throws IOException, BuildStepException {
    Preconditions.checkArgument(Files.isDirectory(directory));
    this.metaDataPath = directory.resolve(METADATA_FILE_NAME);

    Map<String, String> metadata = readMetaData();
    doBuild(directory, metadata);
    writeMetaData(metadata);
  }

  private Map<String, String> readMetaData() throws IOException {
    // load metadata into memory
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(metaDataPath.toFile()))){
      return (HashMap<String, String>) in.readObject();
    } catch (FileNotFoundException e) {
      // return an empty map.
      return new HashMap<>();
    } catch (ClassNotFoundException e) {
      // this should never happen
      throw new RuntimeException(e);
    }
  }

  private void writeMetaData(Map<String, String> metadata) throws IOException {
    // serialize metadata to disk
    try (ObjectOutputStream out = new ObjectOutputStream(
        new FileOutputStream(metaDataPath.toFile()))){
      out.writeObject(metadata);
    }
  }

}
