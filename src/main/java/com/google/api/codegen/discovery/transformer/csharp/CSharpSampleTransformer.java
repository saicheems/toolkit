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
import com.google.api.codegen.discovery.transformer.MethodInfoTransformer;
import com.google.api.codegen.discovery.transformer.SampleTransformer;
import com.google.api.codegen.discovery.viewmodel.LineView;
import com.google.api.codegen.discovery.viewmodel.SampleView;
import com.google.api.codegen.viewmodel.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Transforms a Method and SampleConfig into the standard discovery surface for
 * C#.
 */
public class CSharpSampleTransformer implements SampleTransformer {

  private static final String TEMPLATE_FILENAME = "csharp/sample.snip";

  @Override
  public ViewModel transform(Document document, Method method) {
    return createSampleView(document, method);
  }

  private static SampleView createSampleView(Document document, Method method) {
    SampleView.Builder builder = SampleView.newBuilder();

    builder.apiInfo(ApiInfoTransformer.transform(document));

    builder.appName(""); // TODO

    builder.className(""); // TODO

    List<LineView> fieldLines = new ArrayList<>();
    for (String name : method.parameters().keySet()) {
      Map<String, Schema> properties = method.parameters().get(name).properties();
      fieldLines.add(CSharpLineTransformer.generateFieldLine(method, properties.get(name)));
    }
    builder.fieldLines(fieldLines);

    builder.methodInfo(MethodInfoTransformer.transform(document, method));

    builder.namespaceName(""); // TODO

    builder.nextPageTokenFieldName(""); // TODO

    builder.outputPath(method.id() + ".frag.cs");

    builder.pageStreamingResourceFieldName(""); // TODO

    builder.pageStreamingResourceTypeName(""); // TODO

    builder.pageStreamingResourceVarName(""); // TODO

    builder.pageTokenFieldName("");

    builder.requestBodyLine(CSharpLineTransformer.generateRequestLine(method));

    builder.requestLine(CSharpLineTransformer.generateRequestLine(method));

    builder.responseLine(CSharpLineTransformer.generateRequestLine(method));

    builder.responseVarName(""); // TODO

    builder.serviceTypeName(""); // TODO

    builder.serviceVarName(""); // TODO

    builder.serviceTypeName(""); // TODO

    builder.templateFileName(TEMPLATE_FILENAME);

    List<LineView> usingLines = new ArrayList<>(); // TODO
    builder.usingLines(usingLines);

    return builder.build();
  }
}
