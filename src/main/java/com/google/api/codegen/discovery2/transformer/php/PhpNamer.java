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

import com.google.api.codegen.discovery.Document;
import com.google.api.codegen.discovery.Schema;
import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.List;

public class PhpNamer {

  private final Document document;
  private final String serviceClassName;

  public PhpNamer(Document document) {
    this.document = document;
    this.serviceClassName =
        "Google_Service_" + PhpSymbol.from(document.canonicalName(), true).toUpperCamel().name();
  }

  public String getAppName() {
    return String.format(
        "Google-%sSample/0.1",
        PhpSymbol.from(document.canonicalName(), true).toUpperCamel().name());
  }

  private String getObjectTypeName(Schema schema) {
    StringBuilder typeNameBuilder = new StringBuilder(serviceClassName + "_");
    if (!Strings.isNullOrEmpty(schema.id())) {
      return typeNameBuilder.append(getSafeClassName(schema.id())).toString();
    }
    List<String> segments = Arrays.asList(schema.path().split("\\."));
    // TODO: Replace `segments.indexOf("schemas")` with 0?
    int i = segments.indexOf("schemas") + 1;

    boolean topLevel = true;

    while (i < segments.size()) {
      typeNameBuilder.append(getSafeClassName(segments.get(i)));
      if (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
        if (topLevel) {
          typeNameBuilder.append("Items");
        }
        while (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
          i++;
        }
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("properties")) {
        i++;
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("additionalProperties")) {
        typeNameBuilder.append("Element");
        i++;
      }
      topLevel = false;
      i++;
    }
    return typeNameBuilder.toString();
  }

  public String getRequestBodyVarName() {
    return "requestBody";
  }

  public String getResponseVarName() {
    return "response";
  }

  public String getServiceTypeName() {
    return serviceClassName;
  }

  public String getServiceVarName() {
    return "service";
  }

  public String getTypeName(Schema schema) {
    schema = schema.dereference();
    if (schema.type() == Schema.Type.OBJECT) {
      return getObjectTypeName(schema);
    }
    return "";
  }

  public String getServiceRequestFuncName(String methodPath) {
    StringBuilder typeNameBuilder = new StringBuilder();
    List<String> methodIdSegments = Arrays.asList(methodPath.split("\\."));
    for (int i = 1; i < methodIdSegments.size() - 1; i += 2) {
      typeNameBuilder.append(methodIdSegments.get(i));
      if (i + 2 >= methodIdSegments.size() - 1) {
        typeNameBuilder.append("->");
      } else {
        typeNameBuilder.append("_");
      }
    }
    String methodName = methodIdSegments.get(methodIdSegments.size() - 1);
    typeNameBuilder.append(methodName);
    if (PhpSymbol.from(methodName, true).isReserved()) {
      for (int i = 1; i < methodIdSegments.size() - 1; i += 2) {
        typeNameBuilder.append(getSafeClassName(methodIdSegments.get(i)));
      }
    }
    return typeNameBuilder.toString();
  }

  private String getSafeClassName(String name) {
    String typeName = "";
    PhpSymbol symbol = PhpSymbol.from(name, true);
    if (symbol.isReserved()) {
      typeName += PhpSymbol.from(document.name(), true).toUpperCamel().name();
    }
    return typeName + symbol.toUpperCamel().name();
  }
}
