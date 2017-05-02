/* Copyright 2017 Google Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.discovery.transformer;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.List;

// TODO: Make all containers in autovalues immutable!!!

@AutoValue
public abstract class Path {

  public static Path from(String path) {
    // TODO: Validate? May not be worth it.
    String segments[] = path.split("\\.");
    ImmutableList.Builder<String> methodIdSegments = new ImmutableList.Builder<>();
    for (int i = 1; i < segments.length; i += 2) {
      if (!segments[i - 1].equals("methods") && !segments[i - 1].equals("resources")) {
        break;
      }
      methodIdSegments.add(segments[i]);
    }
    String penultimateSegment = "";
    if (segments.length > 1) {
      penultimateSegment = segments[segments.length - 2];
    }
    return new AutoValue_Path(
        segments[0],
        penultimateSegment,
        segments[segments.length - 1],
        segments.length,
        methodIdSegments.build(),
        ImmutableList.copyOf(segments));
  }

  public abstract String firstSegment();

  public abstract String penultimateSegment();

  public abstract String lastSegment();

  public abstract int length();

  public abstract List<String> methodIdSegments();

  public abstract List<String> segments();
}
