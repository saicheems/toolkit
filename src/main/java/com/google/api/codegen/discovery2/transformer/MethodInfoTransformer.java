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
package com.google.api.codegen.discovery2.transformer;

import com.google.api.codegen.discovery.Method;
import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.discovery2.viewmodel.MethodInfoView;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Set;

public class MethodInfoTransformer {

  private static final ImmutableList<String> PAGE_TOKEN_NAMES =
      ImmutableList.of("pageToken", "nextPageToken");

  public static MethodInfoView transform(Method method) {
    MethodInfoView.Builder builder = MethodInfoView.newBuilder();

    String parametersPageTokenName = getPageTokenNameFromSet(method.parameters().keySet());
    String requestPageTokenName = getPageTokenNameFromSchema(method.request());
    String responsePageTokenName = getPageTokenNameFromSchema(method.response());

    boolean isPageStreaming = false;
    if (!responsePageTokenName.isEmpty()) {
      if (!requestPageTokenName.isEmpty()) {
        isPageStreaming = true;
      } else if (!parametersPageTokenName.isEmpty()) {
        isPageStreaming = true;
      }
    }

    Schema response = method.response();
    String pageStreamingResourceName = getPageStreamingResourceNameFromSchema(response);
    if (response != null && response.dereference().id().equals("Empty")) {
      response = null;
    }

    return builder
        .hasRequestBody(method.request() != null)
        .hasResponse(response != null)
        .isPageStreaming(isPageStreaming)
        .isScopesSingular(method.scopes().size() == 1)
        .pageStreamingResourceDiscoveryFieldName(pageStreamingResourceName)
        .parametersPageTokenDiscoveryFieldName(parametersPageTokenName)
        .requestBodyPageTokenDiscoveryFieldName(requestPageTokenName)
        .responsePageTokenDiscoveryFieldName(responsePageTokenName)
        .scopes(method.scopes())
        .supportsMediaDownload(method.supportsMediaDownload())
        .supportsMediaUpload(method.supportsMediaUpload())
        .verb(method.httpMethod())
        .build();
  }

  private static String getPageTokenNameFromSet(Set<String> names) {
    for (String name : PAGE_TOKEN_NAMES) {
      if (names.contains(name)) {
        return name;
      }
    }
    return "";
  }

  private static String getPageTokenNameFromSchema(Schema schema) {
    if (schema == null) {
      return "";
    }
    schema = schema.dereference();
    for (String name : PAGE_TOKEN_NAMES) {
      if (schema.hasProperty(name)) {
        return name;
      }
    }
    return "";
  }

  private static String getPageStreamingResourceNameFromSchema(Schema schema) {
    if (schema == null) {
      return "";
    }
    schema = schema.dereference();
    for (String name : schema.properties().keySet()) {
      // Check that it's impossible for a member of an object to be defined as repeated, there should only be array types.
      Preconditions.checkState(!schema.properties().get(name).repeated());
    }
    // Priority order:
    // 1. First array of objects.
    // 2. First array or map.
    // 3. First string.

    // Look for the first array of objects.
    for (String name : schema.properties().keySet()) {
      Schema resource = schema.properties().get(name);
      if (resource.type() == Schema.Type.ARRAY
          && resource.items().dereference().type() == Schema.Type.OBJECT) {
        return name;
      }
    }
    // Look for the first array or map resource.
    for (String name : schema.properties().keySet()) {
      Schema resource = schema.properties().get(name);
      if (resource.type() == Schema.Type.ARRAY || resource.additionalProperties() != null) {
        return name;
      }
    }
    // If it's not a repeated resource, look for the first string instead.
    for (String name : schema.properties().keySet()) {
      if (schema.properties().get(name).type() == Schema.Type.STRING) {
        return name;
      }
    }
    return "";
  }
}
