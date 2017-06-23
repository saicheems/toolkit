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

import com.google.api.codegen.discovery.Document;
import com.google.api.codegen.discovery.Method;
import com.google.api.codegen.discovery2.transformer.ApiInfoTransformer;
import com.google.api.codegen.discovery2.transformer.MethodInfoTransformer;
import com.google.api.codegen.discovery2.transformer.SampleTransformer;
import com.google.api.codegen.discovery2.viewmodel.SampleView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.common.base.Preconditions;

import javax.validation.constraints.NotNull;

public class CSharpSampleTransformer implements SampleTransformer {

  @Override
  public ViewModel transform(Method method) {
    return createSampleView(method);
  }

  private static SampleView createSampleView(Method method) {
    Document document = (Document) method.parent();
    Preconditions.checkState(document != null);
    SampleView.Builder builder = SampleView.newBuilder();

    CSharpTypeMap typeMap = new CSharpTypeMap(document);
    SymbolSet symbolSet = new CSharpSymbolSet();

    builder.apiInfo(ApiInfoTransformer.transform(document));
    builder.methodInfo(MethodInfoTransformer.transform(method));

    builder.templateFileName("csharp/discovery_sample.snip");
    builder.outputPath(method.id() + ".frag.cs");

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

    return builder.build();
  }
}
