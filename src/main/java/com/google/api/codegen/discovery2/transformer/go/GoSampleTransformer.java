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
package com.google.api.codegen.discovery2.transformer.go;

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
import java.util.TreeSet;

public class GoSampleTransformer implements SampleTransformer {

  @Override
  public ViewModel transform(Method method, String authInstructionsUrl, JsonNode override) {
    return createSampleView(method, authInstructionsUrl, override);
  }

  private static SampleView createSampleView(
      Method method, String authInstructionsUrl, JsonNode override) {
    Document document = (Document) method.parent();
    Preconditions.checkState(document != null);
    SampleView.Builder builder = SampleView.newBuilder();

    GoTypeMap typeMap = new GoTypeMap(document);
    SymbolSet symbolSet = new GoSymbolSet();

    builder.apiInfo(ApiInfoTransformer.transform(document, authInstructionsUrl));
    MethodInfoView methodInfo = MethodInfoTransformer.transform(method);
    builder.methodInfo(methodInfo);

    GoNamer namer = new GoNamer(document);

    if (methodInfo.hasResponse()) {
      symbolSet.add(typeMap.addImportPath("fmt", false));
    }
    symbolSet.add(typeMap.addImportPath("log", false));
    symbolSet.add(typeMap.addImportPath("golang.org/x/net/context", true));
    switch (document.authType()) {
      case NONE:
        symbolSet.add(typeMap.addImportPath("net/http", false));
        break;
      case ADC:
        symbolSet.add(typeMap.addImportPath("golang.org/x/oauth2/google", true));
        break;
      default:
        symbolSet.add(typeMap.addImportPath("errors", false));
        symbolSet.add(typeMap.addImportPath("net/http", false));
    }

    // Reserve the service's package name.
    symbolSet.add(namer.getServicePackageName());
    // Reserve "err".
    symbolSet.add("err");

    builder.contextVarName(symbolSet.add("ctx"));

    String clientVarName = symbolSet.add("c");
    builder.clientVarName(clientVarName); // TODO: SymbolSet

    builder.getClientFuncName(symbolSet.add("getClient"));

    List<String> scopeConsts = new ArrayList<>();
    switch (document.authType()) {
      case ADC:
        scopeConsts.add(typeMap.addScopeConst("https://www.googleapis.com/auth/cloud-platform"));
        break;
      case OAUTH_3L:
        for (String scope : methodInfo.scopes()) {
          scopeConsts.add(typeMap.addScopeConst(scope));
        }
        break;
    }
    builder.scopeConsts(scopeConsts);

    FieldView service =
        FieldView.empty()
            .withVarName(symbolSet.add(namer.getServiceVarName()))
            .withValue(
                String.format("%s.New(%s)", typeMap.addService().split("\\.")[0], clientVarName));
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

    List<String> argList = new ArrayList<>();
    for (FieldView parameter : parameters) {
      argList.add(parameter.varName());
    }
    if (requestBody != null) {
      argList.add(requestBody.varName());
    }

    builder.request(
        FieldView.empty()
            .withVarName(symbolSet.add(namer.getRequestVarName()))
            .withValue(
                String.format(
                    "%s.%s(%s)",
                    service.varName(),
                    namer.getServiceRequestFuncName(method.path()),
                    Joiner.on(", ").join(argList))));

    if (methodInfo.supportsMediaDownload()) {
      builder.mediaDownloadDocLink(
          String.format(
              "https://godoc.org/google.golang.org/api/%s/%s#%s.Download",
              document.name(), document.version(), typeMap.addRequest(method).split("\\.")[1]));
    }

    Schema responseSchema = method.response();
    if (methodInfo.hasResponse()) {
      String varName;
      if (methodInfo.isPageStreaming()) {
        varName = symbolSet.add("page");
      } else {
        varName = symbolSet.add(namer.getResponseVarName());
      }
      builder.response(
          FieldView.empty().withTypeName(typeMap.add(responseSchema)).withVarName(varName));
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

      if (isArray) {
        pageStreamingResource =
            pageStreamingResource.withTypeName(typeMap.add(pageStreamingResourceSchema.items()));
      } else if (isMap) {
        pageStreamingResource =
            pageStreamingResource
                .withKeyVarName(symbolSet.add("name"))
                .withTypeName(typeMap.add(pageStreamingResourceSchema.additionalProperties()));
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
        String typeNameSegments[] = typeMap.add(element).split("\\.");
        varName = symbolSet.add(typeNameSegments[typeNameSegments.length - 1]);
      } else {
        varName = symbolSet.add(discoveryFieldName);
      }
      builder.pageStreamingResource(
          pageStreamingResource
              .withStructFieldName(typeMap.getStructFieldName(pageStreamingResourceSchema))
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

    List<String> importPaths = new ArrayList<>();
    for (String importPath : new TreeSet<String>(typeMap.getImportPaths().values())) {
      if (!typeMap.isThirdPartyImportPath(importPath)) {
        importPaths.add(importPath);
      }
    }
    builder.standardLibraryImportPaths(importPaths);
    importPaths = new ArrayList<>();
    for (String importPath : new TreeSet<String>(typeMap.getImportPaths().values())) {
      if (typeMap.isThirdPartyImportPath(importPath)) {
        importPaths.add(importPath);
      }
    }
    builder.thirdPartyImportPaths(importPaths);

    builder.templateFileName("go/discovery_sample.snip");
    builder.outputPath(method.id() + ".frag.go");

    return builder.build();
  }
}
