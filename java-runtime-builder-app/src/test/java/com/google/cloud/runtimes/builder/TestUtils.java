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

package com.google.cloud.runtimes.builder;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtils {

  /**
   * Builder class for setting up directories for unit tests.
   */
  public static class TestWorkspaceBuilder {

    private final Path workspaceDir;

    public TestWorkspaceBuilder() throws IOException {
      this.workspaceDir = Files.createTempDirectory(null);
    }

    public FileBuilder file(String path) {
      return new FileBuilder(this, workspaceDir.resolve(path));
    }

    public Path build() {
      return workspaceDir;
    }

    public class FileBuilder {
      private final Path path;
      private final TestWorkspaceBuilder workspaceBuilder;
      private String contents = "";

      private FileBuilder(TestWorkspaceBuilder workspaceBuilder, Path path) {
        this.workspaceBuilder = workspaceBuilder;
        this.path = path;
      }

      public FileBuilder withContents(String contents) {
        this.contents = contents;
        return this;
      }

      public TestWorkspaceBuilder build() throws IOException {
        // mkdir -p
        Files.createDirectories(path.getParent());

        try (Writer out = Files.newBufferedWriter(path, Charset.defaultCharset())) {
          out.write(contents);
        }
        return workspaceBuilder;
      }
    }
  }

}
