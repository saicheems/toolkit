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
package com.google.api.codegen.discovery2.transformer.csharp;

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
import com.google.api.codegen.discovery2.viewmodel.UsingDirectiveView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class CSharpSampleTransformer implements SampleTransformer {

  @Override
  public ViewModel transform(Method method, String authInstructionsUrl, JsonNode override) {
    return createSampleView(method, authInstructionsUrl, override);
  }

  private static SampleView createSampleView(
      Method method, String authInstructionsUrl, JsonNode override) {
    Document document = (Document) method.parent();
    Preconditions.checkState(document != null);
    SampleView.Builder builder = SampleView.newBuilder();

    CSharpTypeMap typeMap = new CSharpTypeMap(document);
    SymbolSet symbolSet = new CSharpSymbolSet();

    builder.apiInfo(ApiInfoTransformer.transform(document, authInstructionsUrl));
    MethodInfoView methodInfo = MethodInfoTransformer.transform(method);
    builder.methodInfo(methodInfo);

    typeMap.addUsingDirective("Google.Apis.Services"); // BaseClientService
    switch (document.authType()) {
      case ADC:
        typeMap.addUsingDirective("Google.Apis.Auth.OAuth2"); // GoogleCredential
        typeMap.addUsingDirective("System.Threading.Tasks"); // Task
        break;
      case OAUTH_3L:
        typeMap.addUsingDirective("Google.Apis.Auth.OAuth2"); // UserCredential
        break;
    }
    // `methodInfo.hasResponse()` also checks whether or not the response ID is "Empty", in which
    // case we don't generate the field.
    if (methodInfo.hasResponse()) {
      typeMap.addUsingDirective("System"); // Console
      typeMap.addUsingDirective("Newtonsoft.Json");
    }

    CSharpNamer namer = new CSharpNamer(document);
    builder.sampleNamespaceName(namer.getSampleNamespaceName());
    builder.sampleClassName(namer.getSampleClassName());

    symbolSet.add("args"); // public static void Main(string[] args)

    // MyService service = new MyService( ...
    FieldView service =
        FieldView.empty()
            .withVarName(symbolSet.add(namer.getServiceVarName()))
            .withTypeName(typeMap.addService());
    builder.service(service);
    //     ApplicationName = "Google-MyServiceSample/0.1",
    builder.appName(namer.getAppName());
    // ... );

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

    List<FieldView> optionalParameters = new ArrayList<>();
    for (Map.Entry<String, Schema> parameter : new TreeMap<>(method.parameters()).entrySet()) {
      JsonNode parameterOverride =
          parametersOverride != null ? parametersOverride.get(parameter.getKey()) : null;
      if (parameter.getValue().required() || parameterOverride == null) {
        continue;
      }
      optionalParameters.add(
          FieldTransformer.transform(parameter.getValue(), symbolSet, typeMap, parameterOverride));
    }
    List<String> argList = new ArrayList<>();
    if (requestBody != null) {
      argList.add(requestBody.varName());
    }
    for (FieldView parameter : parameters) {
      argList.add(parameter.varName());
    }
    builder.request(
        FieldView.empty()
            .withFields(optionalParameters)
            .withTypeName(typeMap.addRequest(method))
            .withVarName(namer.getRequestVarName())
            .withValue(
                String.format(
                    "%s.%s(%s)",
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
      FieldView pageStreamingResource =
          FieldTransformer.transform(pageStreamingResourceSchema, null, typeMap, null);
      String discoveryFieldName = pageStreamingResource.discoveryFieldName();

      String varName;
      if (pageStreamingResourceSchema.additionalProperties() != null) {
        varName = symbolSet.add("item");
      } else if (pageStreamingResourceSchema.items() != null) {
        Schema itemsSchema = pageStreamingResourceSchema.items().dereference();
        switch (itemsSchema.type()) {
          case OBJECT:
          case ARRAY:
            // Derive `varName` from the last segment of the type name so names are generated in-line
            // with the C# types. For example, "jobsData" instead of "jobs" for objects inside arrays.
            String typeNameSegments[] = pageStreamingResource.typeName().split("\\.");
            varName = symbolSet.add(typeNameSegments[typeNameSegments.length - 1]);
            break;
          default:
            varName = symbolSet.add("item");
        }
      } else {
        varName = symbolSet.add(discoveryFieldName);
      }
      builder.pageStreamingResource(pageStreamingResource.withVarName(varName));

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

    List<UsingDirectiveView> usingDirectives = new ArrayList<>();
    List<UsingDirectiveView> usingAliasDirectives = new ArrayList<>();
    for (String namespaceName : new TreeSet<>(typeMap.namespaceNames().keySet())) {
      String alias = typeMap.namespaceNames().get(namespaceName);
      UsingDirectiveView usingDirectiveView = UsingDirectiveView.from(namespaceName, alias);
      if (alias.isEmpty()) {
        usingDirectives.add(usingDirectiveView);
      } else {
        usingAliasDirectives.add(usingDirectiveView);
      }
    }
    builder.usingDirectives(usingDirectives);
    builder.usingAliasDirectives(usingAliasDirectives);

    builder.templateFileName("csharp/discovery_sample.snip");
    builder.outputPath(method.id() + ".frag.cs");

    return builder.build();
  }
}
