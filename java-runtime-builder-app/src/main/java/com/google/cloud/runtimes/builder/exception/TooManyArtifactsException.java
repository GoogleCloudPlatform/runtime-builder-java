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

package com.google.cloud.runtimes.builder.exception;

import com.google.cloud.runtimes.builder.buildsteps.base.BuildStepException;

import java.nio.file.Path;
import java.util.List;

public class TooManyArtifactsException extends BuildStepException {

  public TooManyArtifactsException(List<Path> artifacts) {
    super(buildMessage(artifacts));
  }

  private static String buildMessage(List<Path> artifacts) {
    StringBuilder sb = new StringBuilder();
    sb.append("Ambiguous deployable artifacts were found. Unable to proceed:\n");
    for (Path path : artifacts) {
      sb.append("\t" + path.toString() + "\n");
    }
    return sb.toString();
  }
}
