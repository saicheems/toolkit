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
import com.google.api.codegen.discovery.transformer.ApiInfoTransformer;
import com.google.api.codegen.discovery.transformer.MethodInfoTransformer;
import com.google.api.codegen.discovery.transformer.SampleTransformer;
import com.google.api.codegen.discovery.viewmodel.SampleView;
import com.google.api.codegen.viewmodel.ViewModel;

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

    CSharpSampleMapper mapper = new CSharpSampleMapper(document, method);

    builder.apiInfo(ApiInfoTransformer.transform(document));
    builder.methodInfo(MethodInfoTransformer.transform(document, method));
    builder.namespaceName(mapper.getNamespaceName());
    builder.className(mapper.getClassName());

    builder.templateFileName(TEMPLATE_FILENAME);
    builder.outputPath(method.id() + ".frag.cs");

    return builder.build();
  }
}
