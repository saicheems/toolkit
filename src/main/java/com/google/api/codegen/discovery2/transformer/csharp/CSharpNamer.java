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
import com.google.common.collect.ImmutableTable;
import java.util.List;

public class CSharpNamer {

  // TODO: Move anything related to names here, the getZero value stuff can stay in the type map.

  private final Document document;
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

  public CSharpNamer(Document document) {
    this.document = document;
  }

  public String getSampleNamespaceName() {
    return CSharpSymbol.from(document.canonicalName()).toUpperCamel().name() + "Sample";
  }

  public String getSampleClassName() {
    return CSharpSymbol.from(document.canonicalName()).toUpperCamel().name() + "Example";
  }

  public String getServiceVarName() {
    return CSharpSymbol.from(document.canonicalName()).toLowerCamel().name() + "Service";
  }

  public String getAppName() {
    return String.format(
        "Google-%sSample/0.1", CSharpSymbol.from(document.canonicalName()).toUpperCamel().name());
  }

  public String getRequesVarName() {
    return "request";
  }

  public String getServiceNamespaceName() {
    String canonicalName = document.canonicalName();
    String versionNoDots = document.version().replaceAll("\\.", "_");

    String namespaceName = "Google.Apis";
    namespaceName += "." + CSharpSymbol.from(canonicalName).toUpperCamel().name();
    namespaceName += "." + versionNoDots;
    return namespaceName;
  }

  public String getServiceTypeName() {
    return getSafeClassName(document.canonicalName()) + "Service";
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
        return getObjectTypeName(schema);
    }
    if (schema.type() == Schema.Type.STRING && schema.parent() instanceof Method) {
      // TODO: Actually a special enum.
      String typeName = getSafeClassName();
      return getRequestTypeName(schema.path()) + "." + typeName + "Enum";
    }
    String typeName = typeNames.get(schema.type(), schema.format());
    // TODO: Validate that getTypeName is not null? It shouldn't be possible...
    if (schema.repeated()) {
      typeName = "List<" + typeName + ">";
    }
    return typeName;
  }

  private String getObjectTypeName(Schema schema) {
    if (!schema.id().isEmpty()) {
      return CSharpSymbol.from(schema.id()).toUpperCamel().name();
    }
    Path path = Path.from(schema.path());
    List<String> segments = path.segments();
    // Start at the second segment, which is the first schema key.
    int i = segments.indexOf("schemas") + 1;

    String typeName = "";
    String parentName = "";

    boolean topLevel = true;

    while (i < segments.size()) {
      String childName = CSharpSymbol.from(segments.get(i)).toUpperCamel().name();
      if (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
        if (topLevel) {
          childName += "Items";
        }
        while (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
          i++;
        }
      }
      if (!topLevel) { // ex: "schemas.foo.items.
        // Subclasses have the "Data" suffix.
        childName += "Data";
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("properties")) {
        i++;
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("additionalProperties")) {
        childName += "Element";
        i++;
      }
      if (childName.equals(parentName)) {
        childName += "Schema";
      }
      if (!topLevel) {
        typeName += ".";
      }
      topLevel = false;

      typeName += childName;
      parentName = childName;
      i++;
    }
    return typeName;
  }

  public String getRequestTypeName(Method method) {
    return getRequestTypeName(method.path());
  }

  private String getRequestTypeName(String path) {
    String typeName = "";
    List<String> methodIdSegments = Path.from(path).methodIdSegments();
    for (int i = 0; i < methodIdSegments.size(); i++) {
      typeName += getSafeClassName(methodIdSegments.get(i));
      if (i == methodIdSegments.size() - 1) {
        typeName += "Request";
      } else {
        typeName += "Resource.";
      }
    }
    return typeName;
  }

  public String getServiceRequestFuncFullName(String methodPath) {
    Path path = Path.from(methodPath);
    String typeName = "";
    for (int i = 0; i < path.methodIdSegments().size(); i++) {
      typeName += getSafeClassName(path.methodIdSegments().get(i));
      if (i < path.methodIdSegments().size() - 1) {
        typeName += ".";
      }
    }
    return typeName;
  }

  private String getSafeClassName(String name) {
    String typeName = "";
    CSharpSymbol symbol = CSharpSymbol.from(name);
    if (symbol.isReserved()) {
      typeName += CSharpSymbol.from(document.canonicalName()).toUpperCamel().name();
    }
    return typeName + symbol.toUpperCamel().name();
  }
}