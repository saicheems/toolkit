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
package com.google.api.codegen.discovery2.transformer.java;

import com.google.api.codegen.discovery.Document;
import com.google.api.codegen.discovery.Method;
import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.discovery2.transformer.Symbol;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaNamer {

  private static final ImmutableTable<Schema.Type, Schema.Format, String> BOXED_TYPE_NAMES =
      new ImmutableTable.Builder<Schema.Type, Schema.Format, String>()
          .put(Schema.Type.BOOLEAN, Schema.Format.EMPTY, "java.lang.Boolean")
          .put(Schema.Type.INTEGER, Schema.Format.INT32, "java.lang.Integer")
          .put(Schema.Type.INTEGER, Schema.Format.UINT32, "java.lang.Long")
          .put(Schema.Type.NUMBER, Schema.Format.FLOAT, "java.lang.Float")
          .put(Schema.Type.NUMBER, Schema.Format.DOUBLE, "java.lang.Double")
          .put(Schema.Type.STRING, Schema.Format.INT64, "java.lang.Long")
          .build();

  private static final ImmutableTable<Schema.Type, Schema.Format, String> TYPE_NAMES =
      new ImmutableTable.Builder<Schema.Type, Schema.Format, String>()
          .put(Schema.Type.ANY, Schema.Format.EMPTY, "java.lang.Object")
          .put(Schema.Type.BOOLEAN, Schema.Format.EMPTY, "boolean")
          .put(Schema.Type.INTEGER, Schema.Format.INT32, "int")
          .put(Schema.Type.INTEGER, Schema.Format.UINT32, "long")
          .put(Schema.Type.NUMBER, Schema.Format.FLOAT, "float")
          .put(Schema.Type.NUMBER, Schema.Format.DOUBLE, "double")
          .put(Schema.Type.STRING, Schema.Format.EMPTY, "java.lang.String")
          .put(Schema.Type.STRING, Schema.Format.BYTE, "java.lang.String")
          .put(Schema.Type.STRING, Schema.Format.DATE, "java.lang.String")
          .put(Schema.Type.STRING, Schema.Format.DATETIME, "java.lang.String")
          .put(Schema.Type.STRING, Schema.Format.INT64, "long")
          .put(Schema.Type.STRING, Schema.Format.UINT64, "java.math.BigInteger")
          .build();

  private final Document document;
  private final String packageName;
  private final String serviceClassName;

  public JavaNamer(Document document) {
    this.document = document;
    packageName =
        "com.google.api.services."
            + document.name()
            + (document.versionModule() ? document.version() : "");
    this.serviceClassName = JavaSymbol.from(document.canonicalName()).toUpperCamel().name();
  }

  public String getAppName() {
    return String.format(
        "Google-%sSample/0.1", JavaSymbol.from(document.canonicalName()).toUpperCamel().name());
  }

  private String getObjectTypeName(Schema schema) {
    if (!Strings.isNullOrEmpty(schema.id())) {
      return getSafeClassName(schema.id());
    }
    List<String> segments = Arrays.asList(schema.path().split("\\."));
    // TODO: Replace `segments.indexOf("schemas")` with 0?
    int i = segments.indexOf("schemas") + 1;

    StringBuilder typeNameBuilder = new StringBuilder(packageName + ".");
    List<String> parentTypeNames = new ArrayList<>();

    boolean topLevel = true;

    while (i < segments.size()) {
      String childTypeName = getSafeClassName(segments.get(i));
      if (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
        if (topLevel) {
          childTypeName += "Items";
        }
        while (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
          i++;
        }
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("properties")) {
        i++;
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("additionalProperties")) {
        childTypeName += "Element";
        i++;
      }
      if (parentTypeNames.contains(childTypeName)) {
        childTypeName = Joiner.on("").join(parentTypeNames) + childTypeName;
      }
      if (!topLevel) {
        typeNameBuilder.append("$"); // TODO: Right move?
      }
      topLevel = false;
      typeNameBuilder.append(childTypeName);
      parentTypeNames.add(childTypeName);
      i++;
    }
    return typeNameBuilder.toString();
  }

  public String getRequestTypeName(Method method) {
    return getRequestTypeName(method.path());
  }

  private String getRequestTypeName(String methodPath) {
    StringBuilder typeNameBuilder = new StringBuilder();
    List<String> methodIdSegments = Arrays.asList(methodPath.split("\\."));
    for (int i = 1; i < methodIdSegments.size(); i += 2) {
      typeNameBuilder.append(".");

      Symbol symbol = JavaSymbol.from(methodIdSegments.get(i));
      String segmentTypeName = getSafeClassName(symbol.name());
      if (symbol.name().equals(serviceClassName)) {
        if (i < methodIdSegments.size() - 1) {
          segmentTypeName += "Operations";
        } else { // Last segment.
          segmentTypeName += "Operation";
        }
      }
      typeNameBuilder.append(segmentTypeName);
    }
    return serviceClassName + "." + typeNameBuilder.substring(1);
  }

  public String getRequestVarName() {
    return "request";
  }

  public String getRequestBodyVarName() {
    return "requestBody";
  }

  public String getResponseVarName() {
    return "response";
  }

  public String getSampleClassName() {
    return serviceClassName + "Example";
  }

  public String getTypeName(Schema schema) {
    return getTypeName(schema, false);
  }

  private String getTypeName(Schema schema, boolean boxed) {
    schema = schema.dereference();
    switch (schema.type()) {
      case ARRAY:
        return "java.util.List<" + getTypeName(schema.items(), true) + ">";
      case OBJECT:
        if (schema.additionalProperties() != null) {
          return "java.util.Map<java.lang.String, "
              + getTypeName(schema.additionalProperties(), true)
              + ">";
        }
        return getObjectTypeName(schema);
    }
    String boxedTypeName = BOXED_TYPE_NAMES.get(schema.type(), schema.format());
    if (boxedTypeName != null) {
      return boxedTypeName;
    }
    return TYPE_NAMES.get(schema.type(), schema.format());
  }

  private String getSafeClassName(String name) {
    String typeName = "";
    JavaSymbol symbol = JavaSymbol.from(name);
    if (symbol.isReserved()) {
      typeName += serviceClassName;
    }
    return typeName + symbol.toUpperCamel().name();
  }
}
