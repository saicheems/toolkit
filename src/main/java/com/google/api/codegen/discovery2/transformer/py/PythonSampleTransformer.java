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
package com.google.api.codegen.discovery2.transformer.py;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.codegen.discovery.Document;
import com.google.api.codegen.discovery.Method;
import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.discovery2.transformer.ApiInfoTransformer;
import com.google.api.codegen.discovery2.transformer.FieldTransformer;
import com.google.api.codegen.discovery2.transformer.MethodInfoTransformer;
import com.google.api.codegen.discovery2.transformer.SampleTransformer;
import com.google.api.codegen.discovery2.transformer.SymbolSet;
import com.google.api.codegen.discovery2.viewmodel.FieldView;
import com.google.api.codegen.discovery2.viewmodel.MethodInfoView;
import com.google.api.codegen.discovery2.viewmodel.SampleView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PythonSampleTransformer implements SampleTransformer {

  @Override
  public ViewModel transform(Method method, String authInstructionsUrl, JsonNode override) {
    return createSampleView(method, authInstructionsUrl, override);
  }

  private static SampleView createSampleView(
      Method method, String authInstructionsUrl, JsonNode override) {
    Document document = (Document) method.parent();
    Preconditions.checkState(document != null);
    SampleView.Builder builder = SampleView.newBuilder();

    PythonTypeMap typeMap = new PythonTypeMap(document);
    SymbolSet symbolSet = new PythonSymbolSet();

    builder.apiInfo(ApiInfoTransformer.transform(document, authInstructionsUrl));
    MethodInfoView methodInfo = MethodInfoTransformer.transform(method);
    builder.methodInfo(methodInfo);

    PythonNamer namer = new PythonNamer(document);

    if (methodInfo.hasResponse()) {
      symbolSet.add("pprint");
    }
    if (document.authType() == Document.AuthType.NONE) {
      symbolSet.add("httplib2");
    }
    symbolSet.add("discovery");

    List<String> discoveryBuildParams = new ArrayList<>();
    discoveryBuildParams.add("'" + document.name() + "'");
    discoveryBuildParams.add("'" + document.version() + "'");
    String credentialsVarName = "";
    switch (document.authType()) {
      case NONE:
        break;
      case API_KEY:
        credentialsVarName = symbolSet.add("developerKey");
        discoveryBuildParams.add("developerKey=" + credentialsVarName);
        break;
      default:
        credentialsVarName = symbolSet.add("credentials");
        discoveryBuildParams.add("credentials=" + credentialsVarName);
    }
    builder.credentialsVarName(credentialsVarName);
    FieldView service =
        FieldView.empty()
            .withVarName(namer.getServiceVarName())
            .withValue(
                String.format("discovery.build(%s)", Joiner.on(", ").join(discoveryBuildParams)));
    builder.service(service);

    List<FieldView> parameters = new ArrayList<>();
    JsonNode parametersOverride = override != null ? override.get("parameters") : null;
    for (String name : method.parameterOrder()) {
      JsonNode parameterOverride = parametersOverride != null ? parametersOverride.get(name) : null;
      Schema parameterSchema = method.parameters().get(name);
      parameters.add(
          FieldTransformer.transform(parameterSchema, symbolSet, typeMap, parameterOverride));
    }
    builder.parameters(parameters);

    Schema requestBodySchema = method.request();
    FieldView requestBody = null;
    if (methodInfo.hasRequestBody()) {
      JsonNode requestBodyOverride = override != null ? override.get("requestBody") : null;
      requestBody =
          FieldTransformer.transform(
              requestBodySchema.dereference(),
              symbolSet,
              typeMap,
              requestBodyOverride,
              namer.getRequestBodyVarName(requestBodySchema));
    }
    builder.requestBody(requestBody);

    List<String> argList = new ArrayList<>();
    for (FieldView parameter : parameters) {
      argList.add(
          namer.getParameterName(parameter.discoveryFieldName()) + "=" + parameter.varName());
    }
    if (requestBody != null) {
      argList.add("body=" + requestBody.varName());
    }

    String serviceRequestFuncName = namer.getServiceRequestFuncName(method.path());
    FieldView request =
        FieldView.empty()
            .withVarName(symbolSet.add(namer.getRequestVarName()))
            .withValue(
                service.varName()
                    + "."
                    + serviceRequestFuncName
                    + "("
                    + Joiner.on(", ").join(argList)
                    + ")");
    builder.request(request);
    List<String> serviceRequestFuncNameSegments =
        Arrays.asList(serviceRequestFuncName.split("()\\."));
    builder.lastServiceRequestFuncNameSegment(
        serviceRequestFuncNameSegments.get(serviceRequestFuncNameSegments.size() - 1));

    Schema responseSchema = method.response();
    FieldView response = FieldView.empty().withVarName(symbolSet.add(namer.getResponseVarName()));
    if (methodInfo.hasResponse()) {
      builder.response(response);
    }
    if (methodInfo.isPageStreaming()) {
      Schema pageStreamingResourceSchema =
          responseSchema
              .dereference()
              .properties()
              .get(methodInfo.pageStreamingResourceDiscoveryFieldName());
      //FieldView pageStreamingResource =
      //    FieldTransformer.transform(pageStreamingResourceSchema, null, typeMap, null);
      boolean isArray = pageStreamingResourceSchema.items() != null;
      boolean isMap = pageStreamingResourceSchema.additionalProperties() != null;

      FieldView pageStreamingResource = FieldView.empty().withIsArray(isArray).withIsMap(isMap);

      if (isMap) {
        FieldView pageStreamingResourceKey = FieldView.empty().withVarName(symbolSet.add("name"));
        builder.pageStreamingResourceKey(pageStreamingResourceKey);
      }

      int i = pageStreamingResourceSchema.path().lastIndexOf(".");
      String discoveryFieldName = pageStreamingResourceSchema.path().substring(i + 1);

      Schema element;
      if (isArray) {
        element = pageStreamingResourceSchema.items().dereference();
      } else if (isMap) {
        element = pageStreamingResourceSchema.additionalProperties().dereference();
      } else {
        element = pageStreamingResourceSchema.dereference();
      }

      String varName;
      if (element.type() == Schema.Type.OBJECT) {
        varName = symbolSet.add(element.id());
      } else {
        if (isArray || isMap) {
          varName = symbolSet.add("item");
        } else {
          varName = symbolSet.add(discoveryFieldName);
        }
      }
      builder.pageStreamingResource(
          pageStreamingResource.withDiscoveryFieldName(discoveryFieldName).withVarName(varName));

      Schema pageTokenSchema;
      if (!methodInfo.parametersPageTokenDiscoveryFieldName().isEmpty()) {
        pageTokenSchema =
            method.parameters().get(methodInfo.parametersPageTokenDiscoveryFieldName());
        builder.requestNextPageValue(
            String.format(
                "%s.%s_next(previous_request=%s, previous_response=%s)",
                service.varName(), serviceRequestFuncName, request.varName(), response.varName()));
      } else {
        pageTokenSchema =
            method
                .request()
                .dereference()
                .properties()
                .get(methodInfo.requestBodyPageTokenDiscoveryFieldName());
      }
      builder.pageToken(FieldTransformer.transform(pageTokenSchema, null, typeMap, null));

      Schema nextPageTokenSchema =
          method
              .response()
              .dereference()
              .properties()
              .get(methodInfo.responsePageTokenDiscoveryFieldName());
      builder.nextPageToken(FieldTransformer.transform(nextPageTokenSchema, null, typeMap, null));
    }

    builder.templateFileName("py/discovery_sample.snip");
    builder.outputPath(method.id() + ".frag.py");

    return builder.build();
  }
}
