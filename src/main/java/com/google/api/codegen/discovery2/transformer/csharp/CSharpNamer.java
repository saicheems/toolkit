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

  private static final ImmutableTable<Schema.Type, Schema.Format, String> TYPE_NAMES =
      new ImmutableTable.Builder<Schema.Type, Schema.Format, String>()
          .put(Schema.Type.ANY, Schema.Format.EMPTY, "object")
          .put(Schema.Type.BOOLEAN, Schema.Format.EMPTY, "bool")
          .put(Schema.Type.INTEGER, Schema.Format.INT32, "int")
          .put(Schema.Type.INTEGER, Schema.Format.UINT32, "uint")
          .put(Schema.Type.NUMBER, Schema.Format.FLOAT, "float")
          .put(Schema.Type.NUMBER, Schema.Format.DOUBLE, "double")
          .put(Schema.Type.STRING, Schema.Format.EMPTY, "string")
          .put(Schema.Type.STRING, Schema.Format.BYTE, "string")
          .put(Schema.Type.STRING, Schema.Format.DATE, "string")
          .put(Schema.Type.STRING, Schema.Format.DATETIME, "string")
          .put(Schema.Type.STRING, Schema.Format.INT64, "long")
          .put(Schema.Type.STRING, Schema.Format.UINT64, "ulong")
          .build();

  private final Document document;
  private final String serviceClassName;

  public CSharpNamer(Document document) {
    this.document = document;
    this.serviceClassName = CSharpSymbol.from(document.canonicalName()).toUpperCamel().name();
  }

  public String getAppName() {
    return String.format("Google-%sSample/0.1", serviceClassName);
  }

  private String getObjectTypeName(Schema schema) {
    StringBuilder typeNameBuilder = new StringBuilder("Data.");
    // If the object has an ID, we don't have to do anything special to derive its name.
    if (!Strings.isNullOrEmpty(schema.id())) {
      return typeNameBuilder
          .append(CSharpSymbol.from(schema.id()).toUpperCamel().name())
          .toString();
    }
    List<String> segments = Arrays.asList(schema.path().split("\\."));
    // Start at the second segment, which is the first schema key.
    // Note that the reason we assume that "schemas" is part of path (as opposed to if this
    // schema were a part of the "parameters" schemas) is because anonymous objects cannot be
    // parameters. Therefore all objects must be children of the "schemas" object.
    int i = segments.indexOf("schemas") + 1;

    String parentTypeName = "";

    boolean topLevel = true;

    while (i < segments.size()) {
      String childTypeName = CSharpSymbol.from(segments.get(i)).toUpperCamel().name();
      if (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
        if (topLevel) {
          childTypeName += "Items";
        }
        while (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
          i++;
        }
      }
      if (!topLevel) { // ex: "schemas.foo.items".
        // Subclasses have the "Data" suffix.
        childTypeName += "Data";
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("properties")) {
        i++;
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("additionalProperties")) {
        childTypeName += "Element";
        i++;
      }
      if (childTypeName.equals(parentTypeName)) {
        childTypeName += "Schema";
      }
      if (!topLevel) {
        typeNameBuilder.append(".");
      }
      topLevel = false;
      typeNameBuilder.append(childTypeName);
      parentTypeName = childTypeName;
      i++;
    }
    return typeNameBuilder.toString();
  }

  public String getRequestTypeName(Method method) {
    return getRequestTypeName(method.path());
  }

  private String getRequestTypeName(String methodPath) {
    // Note that we use path because the generator descends the resource hierarchy to build type
    // names, and not the method ID. See "storagetransfer.getGoogleServiceAccount" in
    // "storagetransfer:v1". If the method ID is used, "GetGoogleServiceAccountRequest" is
    // generated instead of "V1.GetGoogleServiceAccountRequest".
    StringBuilder typeNameBuilder = new StringBuilder();
    List<String> methodIdSegments = Arrays.asList(methodPath.split("\\."));
    for (int i = 1; i < methodIdSegments.size(); i += 2) {
      typeNameBuilder.append(".");

      typeNameBuilder.append(getSafeClassName(methodIdSegments.get(i)));
      if (i < methodIdSegments.size() - 1) {
        typeNameBuilder.append("Resource");
      }
    }
    return typeNameBuilder.append("Request").substring(1);
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

  public String getSampleNamespaceName() {
    return serviceClassName + "Sample";
  }

  public String getServiceNamespaceName() {
    String versionNoDots = document.version().replaceAll("\\.", "_");
    return "Google.Apis." + serviceClassName + "." + versionNoDots;
  }

  public String getServiceRequestFuncName(String methodPath) {
    StringBuilder typeNameBuilder = new StringBuilder();
    List<String> methodIdSegments = Arrays.asList(methodPath.split("\\."));
    for (int i = 1; i < methodIdSegments.size(); i += 2) {
      typeNameBuilder.append(getSafeClassName(methodIdSegments.get(i)));
      if (i < methodIdSegments.size() - 1) {
        typeNameBuilder.append(".");
      }
    }
    return typeNameBuilder.toString();
  }

  public String getServiceTypeName() {
    return serviceClassName + "Service";
  }

  public String getServiceVarName() {
    return CSharpSymbol.from(serviceClassName).toLowerCamel().name() + "Service";
  }

  public String getTypeName(Schema schema) {
    schema = schema.dereference();
    switch (schema.type()) {
      default:
        if (!schema.repeated()) {
          break;
        }
        return "List<" + getTypeName(schema.withRepeated(false)) + ">";
      case ARRAY:
        return "List<" + getTypeName(schema.items()) + ">";
      case OBJECT:
        if (schema.additionalProperties() != null) {
          return "Map<String, " + getTypeName(schema.additionalProperties()) + ">";
        }
        return getObjectTypeName(schema);
    }
    if (isSpecialEnum(schema)) {
      // The parent of `schema` must be a `Method`, because only members of the "parameters" field
      // can be special enums.
      String methodId = ((Method) schema.parent()).path();
      String segments[] = schema.path().split("\\.");
      String typeName = getSafeClassName(segments[segments.length - 1]);
      return getRequestTypeName(methodId) + "." + typeName + "Enum";
    }
    return TYPE_NAMES.get(schema.type(), schema.format());
  }

  public String getTopLevelEnumValueName(String value) {
    return getSafeClassName(value);
  }

  // TODO: Rename to isTopLevelEnum.
  public static boolean isSpecialEnum(Schema schema) {
    String segments[] = schema.path().split("\\.");
    return schema.isEnum() && segments[segments.length - 2].equals("parameters");
  }

  private String getSafeClassName(String name) {
    String typeName = "";
    CSharpSymbol symbol = CSharpSymbol.from(name);
    if (symbol.isReserved()) {
      typeName += serviceClassName;
    }
    return typeName + symbol.toUpperCamel().name();
  }
}
