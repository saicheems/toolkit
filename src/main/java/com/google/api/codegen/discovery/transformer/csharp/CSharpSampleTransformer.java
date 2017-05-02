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
package com.google.api.codegen.discovery.transformer.csharp;

import com.google.api.codegen.discovery.Document;
import com.google.api.codegen.discovery.Method;
import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.discovery.transformer.ApiInfoTransformer;
import com.google.api.codegen.discovery.transformer.FieldTransformer;
import com.google.api.codegen.discovery.transformer.MethodInfoTransformer;
import com.google.api.codegen.discovery.transformer.SampleTransformer;
import com.google.api.codegen.discovery.transformer.SymbolSet;
import com.google.api.codegen.discovery.viewmodel.FieldView;
import com.google.api.codegen.discovery.viewmodel.SampleView;
import com.google.api.codegen.discovery.viewmodel.UsingDirectiveView;
import com.google.api.codegen.viewmodel.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/*
 * Transforms a Method and SampleConfig into the standard discovery surface for
 * C#.
 */
public class CSharpSampleTransformer implements SampleTransformer {

  @Override
  public ViewModel transform(Document document, Method method) {
    return createSampleView(document, method);
  }

  private static SampleView createSampleView(Document document, Method method) {
    SampleView.Builder builder = SampleView.newBuilder();

    CSharpTypeMap typeMap = new CSharpTypeMap(document);
    SymbolSet symbolSet = new CSharpSymbolSet();

    builder.apiInfo(ApiInfoTransformer.transform(document));
    builder.methodInfo(MethodInfoTransformer.transform(document, method));

    typeMap.addUsingDirective("Google.Apis.Services"); // BaseClientService
    switch (document.authType()) {
      case ADC:
        typeMap.addUsingDirective("Google.Apis.Auth.OAuth2"); // GoogleCredential
        typeMap.addUsingDirective("System.Threading.Tasks.Task"); // Task
        break;
      case OAUTH_3L:
        typeMap.addUsingDirective("Google.Apis.Auth.OAuth2"); // UserCredential
        break;
    }
    if (method.response() != null) {
      typeMap.addUsingDirective("System"); // Console
      typeMap.addUsingDirective("Newtonsoft.Json");
    }

    String safeCanonicalName = CSharpSymbol.from(document.canonicalName()).toUpperCamel().name();
    builder.sampleNamespaceName(safeCanonicalName + "Sample");
    builder.sampleClassName(safeCanonicalName + "Example");

    symbolSet.add(CSharpSymbol.from("args")); // public static void Main(string[] args)

    // MyService service = new MyService( ...
    FieldView service =
        FieldView.empty().withVarName(symbolSet.add("service")).withTypeName(typeMap.addService());
    builder.service(service);
    //     ApplicationName = "Google-MyServiceSample/0.1",
    builder.appName(String.format("Google-%sSample/0.1", safeCanonicalName));
    // ... );

    List<FieldView> parameters = new ArrayList<>();
    for (String name : method.parameterOrder()) {
      Schema parameterSchema = document.dereferenceSchema(method.parameters().get(name));
      parameters.add(FieldTransformer.transform(parameterSchema, typeMap, symbolSet));
    }
    builder.parameters(parameters);

    if (method.request() != null) {
      Schema requestBodySchema = document.dereferenceSchema(method.request());
      builder.requestBody(FieldTransformer.transform(requestBodySchema, typeMap, symbolSet));
    }

    builder.request(
        FieldView.empty()
            .withVarName(symbolSet.add("request"))
            .withTypeName(typeMap.addRequest(method)));

    CSharpNamer namer = new CSharpNamer(document.canonicalName());
    builder.serviceRequestFullFuncName(namer.getServiceRequestFuncFullName(method.path()));

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

    //

    // for each parameter
    //   transform parameter
    //   set var name
    //   set type name
    //   change type table

    // for each request body field
    //   transform request body field
    //   set var name
    //   set type name
    //   change type table

    //

    builder.templateFileName("csharp/sample.snip");
    builder.outputPath(method.id() + ".frag.cs");

    return builder.build();
  }
}
