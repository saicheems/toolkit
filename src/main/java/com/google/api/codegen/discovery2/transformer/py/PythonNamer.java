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

import com.google.api.codegen.discovery.Document;
import com.google.api.codegen.discovery.Schema;
import java.util.Arrays;
import java.util.List;

public class PythonNamer {

  private final Document document;

  public PythonNamer(Document document) {
    this.document = document;
  }

  public String getServiceVarName() {
    return "service";
  }

  public String getRequestBodyVarName(Schema schema) {
    return PythonSymbol.from(schema.dereference().id()).toLowerUnderscore().name() + "_body";
  }

  public String getServiceRequestFuncName(String methodPath) {
    StringBuilder typeNameBuilder = new StringBuilder();
    List<String> methodIdSegments = Arrays.asList(methodPath.split("\\."));
    for (int i = 1; i < methodIdSegments.size(); i += 2) {
      String name = methodIdSegments.get(i);
      if (PythonSymbol.from(name).isKeyword()) {
        typeNameBuilder.append(name + "_");
      } else {
        typeNameBuilder.append(name);
      }
      if (i < methodIdSegments.size() - 1) {
        typeNameBuilder.append("().");
      }
    }
    return typeNameBuilder.toString();
  }

  public String getRequestVarName() {
    return "request";
  }

  public String getParameterName(String name) {
    StringBuilder resultBuilder = new StringBuilder();
    if (!Character.isAlphabetic(name.charAt(0))) {
      resultBuilder.append("x");
    }
    for (int i = 0; i < name.length(); i++) {
      if (Character.isLetterOrDigit(name.charAt(i))) {
        resultBuilder.append(name.charAt(i));
      } else {
        resultBuilder.append("_");
      }
    }
    return resultBuilder.toString();
  }

  public String getResponseVarName() {
    return "response";
  }
}
