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
package com.google.api.codegen.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A representation of a Discovery Document method.
 *
 * <p>Note that this class is not necessarily a 1-1 mapping of the official specification.
 */
@AutoValue
public abstract class Method implements Comparable<Method> {

  /**
   * Returns a method constructed from root and resourceHierarchy.
   *
   * @param root the root node to parse.
   * @param resourceHierarchy an in-order list of parent resources, or an empty list if none.
   * @return a method.
   */
  public static Method from(DiscoveryNode root, List<String> resourceHierarchy) {
    String description = root.getString("description");
    String httpMethod = root.getString("httpMethod");
    String id = root.getString("id");
    List<String> parameterOrder = new ArrayList<>();
    for (DiscoveryNode nameNode : root.getArray("parameterOrder").elements()) {
      parameterOrder.add(nameNode.asText());
    }

    DiscoveryNode parametersNode = root.getObject("parameters");
    // Since parameterOrder and parameters should mirror one another, we perform a series of checks to ensure they contain the same names.
    if (parametersNode.isEmpty()) {
      // 1. if parameters is empty there should not be any names in parameterOrder.
      Preconditions.checkState(parameterOrder.isEmpty());
    } else {
      // 2. parameters and parameterOrder should always be the same size.
      Preconditions.checkState(parametersNode.size() == parameterOrder.size());
      // 3. parameters and parameterOrder should have the same names.
      Preconditions.checkState(parametersNode.fieldNames().containsAll(parameterOrder));
    }
    ImmutableMap.Builder<String, Schema> parametersBuilder = ImmutableMap.builder();
    for (String name : parameterOrder) {
      parametersBuilder.put(name, Schema.from(parametersNode.getObject(name)));
    }

    Schema request = Schema.from(root.getObject("request"));
    Schema response = Schema.from(root.getObject("response"));
    List<String> scopes = new ArrayList<>();
    for (DiscoveryNode scopeNode : root.getArray("scopes").elements()) {
      scopes.add(scopeNode.asText());
    }
    boolean supportsMediaDownload = root.getBoolean("supportsMediaDownload");
    boolean supportsMediaUpload = root.getBoolean("supportsMediaUpload");

    return new AutoValue_Method(
        description,
        httpMethod,
        id,
        parametersBuilder.build(),
        request,
        ImmutableList.copyOf(resourceHierarchy),
        response,
        scopes,
        supportsMediaDownload,
        supportsMediaUpload);
  }

  @Override
  public int compareTo(Method other) {
    return id().compareTo(other.id());
  }

  /** @return the description. */
  @JsonProperty("description")
  public abstract String description();

  /** @return the HTTP method. */
  @JsonProperty("httpMethod")
  public abstract String httpMethod();

  /** @return the ID. */
  @JsonProperty("id")
  public abstract String id();

  /** @return the map of parameter names to schemas. */
  @JsonProperty("parameters")
  public abstract ImmutableMap<String, Schema> parameters();

  /** @return the request schema, or null if none. */
  @JsonProperty("request")
  public abstract Schema request();

  /** @return the in-order list of parent resources. */
  @JsonProperty("resourceHierarchy")
  public abstract List<String> resourceHierarchy();

  /** @return the response schema, or null if none. */
  @JsonProperty("response")
  public abstract Schema response();

  /** @return the list of scopes. */
  @JsonProperty("scopes")
  public abstract List<String> scopes();

  /** @return whether or not the method supports media download. */
  @JsonProperty("supportsMediaDownload")
  public abstract boolean supportsMediaDownload();

  /** @return whether or not the method supports media upload. */
  @JsonProperty("supportsMediaUpload")
  public abstract boolean supportsMediaUpload();
}
