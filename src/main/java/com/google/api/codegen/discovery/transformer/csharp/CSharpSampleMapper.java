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
import com.google.api.codegen.discovery.transformer.SampleMapper;

public class CSharpSampleMapper extends SampleMapper {

  private final Document document;
  private final Method method;

  public CSharpSampleMapper(Document document, Method method) {
    this.document = document;
    this.method = method;
  }

  public String getNamespaceName() {
    return upperCamelCase(document.canonicalName() + "Sample");
  }

  public String getClassName() {
    return upperCamelCase(document.canonicalName()) + "Example";
  }

  public String getTypeName(Schema schema) {
    return "";
  }

  public String getZeroValue(Schema schema) {
    return "";
  }

  private String upperCamelCase(String name) {
    name = name.replaceAll(" ", "");
    String pieces[] = name.split("[\\./-]+");
    name = "";
    for (String p : pieces) {
      if (p.isEmpty()) {
        continue;
      }
      name += p.substring(0, 1).toUpperCase() + p.substring(1);
    }
    return name;
  }
}
