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

package com.google.cloud.runtimes.builder.config.domain;

import com.google.common.base.MoreObjects;

import java.nio.file.Files;
import java.nio.file.Path;

public class Artifact {

  /**
   * Returns an {@link Artifact} of the appropriate type for the given path. If the artifact cannot
   * be identified, a {@code IllegalArgumentException} is thrown. The path need not point to an
   * existing file.
   */
  public static Artifact fromPath(Path path) {
    String extension = com.google.common.io.Files.getFileExtension(path.toString());

    if (Files.exists(path.resolve("WEB-INF"))) {
      if (Files.exists(path.resolve("WEB-INF/appengine-web.xml"))) {
        return new Artifact(ArtifactType.COMPAT_EXPLODED_WAR, path);
      }
      return new Artifact(ArtifactType.EXPLODED_WAR, path);

    } else if (extension.equalsIgnoreCase("war")) {
      return new Artifact(ArtifactType.WAR, path);

    } else if (extension.equalsIgnoreCase("jar")) {
      return new Artifact(ArtifactType.JAR, path);

    } else {
      throw new IllegalArgumentException("The file at path " + path + " is not a valid Java "
          + "artifact. Expected a JAR, WAR, or exploded WAR artifact");
    }
  }

  private final Path path;
  private final ArtifactType type;

  public Artifact(ArtifactType type, Path path) {
    this.path = path;
    this.type = type;
  }

  public Path getPath() {
    return path;
  }

  public ArtifactType getType() {
    return type;
  }

  /**
   * Returns true if the given {@code path} is a recognized artifact type, false if otherwise.
   * See also {@link Artifact#fromPath(Path)}.
   */
  public static boolean isAnArtifact(Path path) {
    try {
      // if this method doesn't throw an exception, it is a valid artifact
      fromPath(path);
      return true;

    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public enum ArtifactType {
    JAR, WAR, EXPLODED_WAR, COMPAT_EXPLODED_WAR
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("type", type)
        .add("path", path)
        .toString();
  }
}

