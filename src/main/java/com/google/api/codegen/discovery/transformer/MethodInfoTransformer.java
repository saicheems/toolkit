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
import java.util.Set;

public class MethodInfoTransformer {

  private static final ImmutableList<String> PAGE_TOKEN_NAMES =
      ImmutableList.of("pageToken", "nextPageToken");

  public static MethodInfoView transform(Document document, Method method) {
    MethodInfoView.Builder builder = MethodInfoView.newBuilder();

    String parametersPageTokenName = getPageTokenNameFromSet(method.parameters().keySet());
    String requestPageTokenName = getPageTokenNameFromSchema(document, method.request());
    String responsePageTokenName = getPageTokenNameFromSchema(document, method.response());

    boolean isPageStreaming = false;
    if (!responsePageTokenName.isEmpty()) {
      if (!requestPageTokenName.isEmpty()) {
        isPageStreaming = true;
      } else if (!parametersPageTokenName.isEmpty()) {
        isPageStreaming = true;
      }
    }

    Schema response = document.dereferenceSchema(method.response());
    String pageStreamingResourceName = getPageStreamingResourceNameFromSchema(document, response);
    boolean isPageStreamingResourceRepeated = false;
    if (!pageStreamingResourceName.isEmpty()) {
      Schema pageStreamingResource = response.properties().get(pageStreamingResourceName);
      if (pageStreamingResource.type() == Schema.Type.ARRAY || pageStreamingResource.repeated()) {
        isPageStreamingResourceRepeated = true;
      }
    }

    return builder
        .hasRequestBody(method.request() != null)
        .hasResponse(method.response() != null)
        .isPageStreaming(isPageStreaming)
        .isPageStreamingResourceRepeated(isPageStreamingResourceRepeated)
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

  private static String getPageTokenNameFromSchema(Document document, Schema schema) {
    if (schema == null) {
      return "";
    }
    schema = document.dereferenceSchema(schema);
    for (String name : PAGE_TOKEN_NAMES) {
      if (schema.hasProperty(name)) {
        return name;
      }
    }
    return "";
  }

  private static String getPageStreamingResourceNameFromSchema(Document document, Schema schema) {
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