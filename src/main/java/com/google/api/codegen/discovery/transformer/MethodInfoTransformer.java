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

import com.google.api.codegen.discovery.Document;
import com.google.api.codegen.discovery.Method;
import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.discovery.viewmodel.MethodInfoView;
import com.google.common.collect.ImmutableList;

import javax.validation.constraints.NotNull;
import java.util.Set;

public class MethodInfoTransformer {

  private static final ImmutableList<String> PAGE_TOKEN_NAMES =
      ImmutableList.of("pageToken", "nextPageToken");

  public static MethodInfoView transform(Document document, Method method) {
    MethodInfoView.Builder builder = MethodInfoView.newBuilder();

    String parametersPageTokenPropertyName = getPageTokenNameFromSet(method.parameters().keySet());
    String requestPageTokenPropertyName = getPageTokenNameFromSchema(document, method.request());
    String responsePageTokenPropertyName = getPageTokenNameFromSchema(document, method.response());

    boolean isPageStreaming = false;
    if (!responsePageTokenPropertyName.isEmpty()) {
      if (!requestPageTokenPropertyName.isEmpty()) {
        isPageStreaming = true;
      } else if (!parametersPageTokenPropertyName.isEmpty()) {
        isPageStreaming = true;
      }
    }

    Schema response = document.dereferenceSchema(method.response());
    String pageStreamingResourcePropertyName =
        getPageStreamingResourcePropertyNameFromSchema(document, response);
    Schema.Type pageStreamingResourceType;
    if (pageStreamingResourcePropertyName.isEmpty()) {
      pageStreamingResourceType = Schema.Type.EMPTY;
    } else {
      pageStreamingResourceType =
          response.properties().get(pageStreamingResourcePropertyName).type();
    }

    return builder
        .hasRequestBody(method.request().type() != Schema.Type.EMPTY)
        .hasResponse(method.response().type() != Schema.Type.EMPTY)
        .isPageStreaming(isPageStreaming)
        .isScopesSingular(method.scopes().size() == 1)
        .pageStreamingResourcePropertyName(pageStreamingResourcePropertyName)
        .pageStreamingResourceType(pageStreamingResourceType)
        .parametersPageTokenPropertyName(parametersPageTokenPropertyName)
        .requestBodyPageTokenPropertyName(requestPageTokenPropertyName)
        .responsePageTokenPropertyName(responsePageTokenPropertyName)
        .scopes(method.scopes())
        .supportsMediaDownload(method.supportsMediaDownload())
        .supportsMediaUpload(method.supportsMediaUpload())
        .build();
  }

  private static String getPageTokenNameFromSet(@NotNull Set<String> names) {
    for (String pageTokenName : PAGE_TOKEN_NAMES) {
      if (names.contains(pageTokenName)) {
        return pageTokenName;
      }
    }
    return "";
  }

  private static String getPageTokenNameFromSchema(Document document, Schema schema) {
    if (schema == null) {
      return "";
    }
    schema = document.dereferenceSchema(schema);
    for (String pageTokenName : PAGE_TOKEN_NAMES) {
      if (schema.hasProperty(pageTokenName)) {
        return pageTokenName;
      }
    }
    return "";
  }

  private static String getPageStreamingResourcePropertyNameFromSchema(
      Document document, Schema schema) {
    if (schema == null) {
      return "";
    }
    schema = document.dereferenceSchema(schema);

    // Look for the first repeated resource.
    for (String name : schema.properties().keySet()) {
      if (schema.properties().get(name).repeated()) {
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
