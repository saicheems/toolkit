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
import com.google.api.codegen.discovery.Schema;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableTable;
import java.util.Arrays;
import java.util.List;

public class CSharpNamer {

  // TODO: Move anything related to names here, the getZero value stuff can stay in the type map.

  private static final ImmutableTable<Schema.Type, Schema.Format, String> typeNames =
      new ImmutableTable.Builder<Schema.Type, Schema.Format, String>()
          .put(Schema.Type.ANY, Schema.Format.EMPTY, "object")
          .put(Schema.Type.BOOLEAN, Schema.Format.EMPTY, "bool")
          .put(Schema.Type.INTEGER, Schema.Format.INT32, "int")
          .put(Schema.Type.INTEGER, Schema.Format.UINT32, "uint")
          .put(Schema.Type.NUMBER, Schema.Format.FLOAT, "float")
          .put(Schema.Type.NUMBER, Schema.Format.DOUBLE, "double")
          .put(Schema.Type.STRING, Schema.Format.EMPTY, "string")
          .put(Schema.Type.STRING, Schema.Format.BYTE, "byte")
          .put(Schema.Type.STRING, Schema.Format.DATE, "string")
          .put(Schema.Type.STRING, Schema.Format.DATETIME, "string")
          .put(Schema.Type.STRING, Schema.Format.INT64, "long")
          .put(Schema.Type.STRING, Schema.Format.UINT64, "ulong")
          .build();
  private final Document document;

  public CSharpNamer(Document document) {
    this.document = document;
  }

  public String getAppName() {
    return String.format(
        "Google-%sSample/0.1", CSharpSymbol.from(document.canonicalName()).toUpperCamel().name());
  }

  private String getObjectTypeName(Schema schema) {
    // If the object has an ID, we don't have to do anything special to derive its name.
    if (!Strings.isNullOrEmpty(schema.id())) {
      return CSharpSymbol.from(schema.id()).toUpperCamel().name();
    }
    List<String> segments = Arrays.asList(schema.path().split("\\."));
    // Start at the second segment, which is the first schema key.
    // Note that the reason we assume that "schemas" is part of path (as opposed to if this
    // schema were a part of the "parameters" schemas) is because anonymous objects cannot be
    // parameters. Therefore all objects must be children of the "schemas" object.
    int i = segments.indexOf("schemas") + 1;

    StringBuilder typeName = new StringBuilder();
    String parentName = "";

    boolean topLevel = true;

    while (i < segments.size()) {
      StringBuilder childName =
          new StringBuilder(CSharpSymbol.from(segments.get(i)).toUpperCamel().name());
      if (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
        if (topLevel) {
          childName.append("Items");
        }
        while (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
          i++;
        }
      }
      if (!topLevel) { // ex: "schemas.foo.items".
        // Subclasses have the "Data" suffix.
        childName.append("Data");
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("properties")) {
        i++;
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("additionalProperties")) {
        childName.append("Element");
        i++;
      }
      if (childName.toString().equals(parentName)) {
        childName.append("Schema");
      }
      if (!topLevel) {
        typeName.append(".");
      }
      topLevel = false;

      typeName.append(childName);
      parentName = childName.toString();
      i++;
    }
    return typeName.toString();
  }

  public String getRequestTypeName(Method method) {
    return getRequestTypeName(method.path());
  }

  private String getRequestTypeName(String methodPath) {
    // Note that we use path because the generator descends the resource hierarchy to build type
    // names, and not the method ID. See "storagetransfer.getGoogleServiceAccount" in
    // "storagetransfer:v1". If the method ID is used, "GetGoogleServiceAccountRequest" is
    // generated instead of "V1.GetGoogleServiceAccountRequest".
    StringBuilder typeName = new StringBuilder();
    List<String> methodIdSegments = Arrays.asList(methodPath.split("\\."));
    for (int i = 1; i < methodIdSegments.size(); i += 2) {
      typeName.append(getSafeClassName(methodIdSegments.get(i)));
      if (i == methodIdSegments.size() - 1) {
        typeName.append("Request");
      } else {
        typeName.append("Resource.");
      }
    }
    return typeName.toString();
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

  private String getSafeClassName(String name) {
    String typeName = "";
    CSharpSymbol symbol = CSharpSymbol.from(name);
    if (symbol.isReserved()) {
      typeName += CSharpSymbol.from(document.canonicalName()).toUpperCamel().name();
    }
    return typeName + symbol.toUpperCamel().name();
  }

  public String getSampleClassName() {
    return CSharpSymbol.from(document.canonicalName()).toUpperCamel().name() + "Example";
  }

  public String getSampleNamespaceName() {
    return CSharpSymbol.from(document.canonicalName()).toUpperCamel().name() + "Sample";
  }

  public String getServiceNamespaceName() {
    String canonicalName = document.canonicalName();
    String versionNoDots = document.version().replaceAll("\\.", "_");

    String namespaceName = "Google.Apis";
    namespaceName += "." + CSharpSymbol.from(canonicalName).toUpperCamel().name();
    namespaceName += "." + versionNoDots;
    return namespaceName;
  }

  public String getServiceRequestFuncName(String methodPath) {
    StringBuilder typeName = new StringBuilder();
    List<String> methodIdSegments = Arrays.asList(methodPath.split("\\."));
    for (int i = 1; i < methodIdSegments.size(); i += 2) {
      typeName.append(getSafeClassName(methodIdSegments.get(i)));
      if (i < methodIdSegments.size() - 1) {
        typeName.append(".");
      }
    }
    return typeName.toString();
  }

  public String getServiceTypeName() {
    return getSafeClassName(document.canonicalName()) + "Service";
  }

  public String getServiceVarName() {
    return CSharpSymbol.from(document.canonicalName()).toLowerCamel().name() + "Service";
  }

  public String getTypeName(Schema schema) {
    schema = schema.dereference();
    switch (schema.type()) {
      case ARRAY:
        return "List<" + getTypeName(schema.items()) + ">";
      case OBJECT:
        if (schema.additionalProperties() != null) {
          return "Map<String, " + getTypeName(schema.additionalProperties()) + ">";
        }
        // All objects are under the alias "Data".
        return "Data." + getObjectTypeName(schema);
    }
    if (isSpecialEnum(schema)) {
      // The parent of `schema` must be a `Method`, because only members of the "parameters" field
      // can be special enums.
      String methodId = ((Method) schema.parent()).path();
      String segments[] = schema.path().split("\\.");
      String typeName = getSafeClassName(segments[segments.length - 1]);
      return getRequestTypeName(methodId) + "." + typeName + "Enum";
    }
    String typeName = typeNames.get(schema.type(), schema.format());
    return typeName;
  }

  public static boolean isSpecialEnum(Schema schema) {
    String segments[] = schema.path().split("\\.");
    return schema.isEnum() && segments[segments.length - 2].equals("parameters");
  }
}
