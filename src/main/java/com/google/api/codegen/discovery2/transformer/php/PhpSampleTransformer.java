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
package com.google.api.codegen.discovery2.transformer.php;

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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PhpSampleTransformer implements SampleTransformer {
  @Override
  public ViewModel transform(Method method, String authInstructionsUrl, JsonNode override) {
    return createSampleView(method, authInstructionsUrl, override);
  }

  private static SampleView createSampleView(
      Method method, String authInstructionsUrl, JsonNode override) {
    Document document = (Document) method.parent();
    Preconditions.checkState(document != null);
    SampleView.Builder builder = SampleView.newBuilder();

    PhpTypeMap typeMap = new PhpTypeMap(document);
    SymbolSet symbolSet = new PhpSymbolSet();

    builder.apiInfo(ApiInfoTransformer.transform(document, authInstructionsUrl));
    MethodInfoView methodInfo = MethodInfoTransformer.transform(method);
    builder.methodInfo(methodInfo);

    PhpNamer namer = new PhpNamer(document);
    builder.clientVarName(symbolSet.add("client"));
    builder.appName(namer.getAppName());

    FieldView service =
        FieldView.empty()
            .withVarName(symbolSet.add(namer.getServiceVarName()))
            .withTypeName(typeMap.addService());
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
              namer.getRequestBodyVarName());
    }
    builder.requestBody(requestBody);

    List<FieldView> optParamsFields = new ArrayList<>();
    for (Map.Entry<String, Schema> parameter : new TreeMap<>(method.parameters()).entrySet()) {
      JsonNode parameterOverride =
          parametersOverride != null ? parametersOverride.get(parameter.getKey()) : null;
      if (parameter.getValue().required() || parameterOverride == null) {
        continue;
      }
      optParamsFields.add(
          FieldTransformer.transform(parameter.getValue(), symbolSet, typeMap, parameterOverride));
    }
    FieldView optParams = FieldView.empty();
    if (optParamsFields.size() > 0
        || methodInfo.supportsMediaDownload()
        || (methodInfo.isPageStreaming()
            && !methodInfo.parametersPageTokenDiscoveryFieldName().isEmpty())) {
      optParams =
          optParams
              .withVarName(symbolSet.add("optParams"))
              .withIsArray(true)
              .withValue("array()")
              .withFields(optParamsFields);
    }
    builder.optParams(optParams);

    List<String> argList = new ArrayList<>();
    for (FieldView parameter : parameters) {
      argList.add("$" + parameter.varName());
    }
    if (requestBody != null) {
      argList.add("$" + requestBody.varName());
    }
    if (!optParams.varName().isEmpty()) {
      argList.add("$" + optParams.varName());
    }
    builder.request(
        FieldView.empty()
            .withValue(
                String.format(
                    "$%s->%s(%s)",
                    service.varName(),
                    namer.getServiceRequestFuncName(method.path()),
                    Joiner.on(", ").join(argList))));

    Schema responseSchema = method.response();
    if (methodInfo.hasResponse()) {
      builder.response(
          FieldView.empty()
              .withVarName(namer.getResponseVarName())
              .withTypeName(typeMap.add(responseSchema.dereference())));
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

      FieldView pageStreamingResource =
          FieldView.empty()
              .withIsArray(isArray)
              .withIsMap(isMap)
              .withTypeName(namer.getTypeName(pageStreamingResourceSchema));

      if (isArray) {
        pageStreamingResource =
            pageStreamingResource.withTypeName(typeMap.add(pageStreamingResourceSchema.items()));
      } else if (isMap) {
        pageStreamingResource =
            pageStreamingResource.withTypeName(
                typeMap.add(pageStreamingResourceSchema.additionalProperties()));

        FieldView pageStreamingResourceKey = FieldView.empty().withVarName(symbolSet.add("name"));
        builder.pageStreamingResourceKey(pageStreamingResourceKey);
      } else {
        pageStreamingResource =
            pageStreamingResource.withTypeName(typeMap.add(pageStreamingResourceSchema));
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
        String typeNameSegments[] = typeMap.add(element).split("_");
        varName = symbolSet.add(typeNameSegments[typeNameSegments.length - 1]);
      } else {
        varName = symbolSet.add(discoveryFieldName);
      }
      builder.pageStreamingResource(
          pageStreamingResource
              .withDiscoveryFieldName(discoveryFieldName)
              //.withGetterFuncName(typeMap.getGetterFuncName(pageStreamingResourceSchema))
              .withVarName(varName));

      Schema pageTokenSchema;
      if (!methodInfo.parametersPageTokenDiscoveryFieldName().isEmpty()) {
        pageTokenSchema =
            method.parameters().get(methodInfo.parametersPageTokenDiscoveryFieldName());
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

    builder.templateFileName("php/discovery_sample.snip");
    builder.outputPath(method.id() + ".frag.php");

    return builder.build();
  }
}
