/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.runtimes.builder.buildsteps.base;

import com.google.common.base.MoreObjects;
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

  /**
   * Executes the build step on the given directory.
   *
   * @throws IOException if a transient file system error was encountered
   * @throws BuildStepException if an exception occurred while executing the build step
   */
  public void run(Path directory) throws IOException, BuildStepException {
    Preconditions.checkArgument(Files.isDirectory(directory));
    this.metaDataPath = directory.resolve(METADATA_FILE_NAME);

    Map<String, String> metadata = readMetaData();
    doBuild(directory, metadata);
    writeMetaData(metadata);
  }

  private Map<String, String> readMetaData() throws IOException {
    // load metadata into memory
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(metaDataPath.toFile()))) {
      return (HashMap<String, String>) in.readObject();
    } catch (FileNotFoundException e) {
      // if the metadata file is not found, this is equivalent to an empty map
      return new HashMap<>();
    } catch (ClassNotFoundException e) {
      // this should never happen
      throw new RuntimeException(e);
    }
  }

  private void writeMetaData(Map<String, String> metadata) throws IOException {
    // serialize metadata to disk
    try (ObjectOutputStream out = new ObjectOutputStream(
        new FileOutputStream(metaDataPath.toFile()))) {
      out.writeObject(metadata);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }

}
