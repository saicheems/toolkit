/* Copyright 2016 Google Inc
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
package com.google.api.codegen.discovery2.transformer.go;

import com.google.api.codegen.discovery.Document;
import com.google.api.codegen.discovery.Method;
import com.google.api.codegen.discovery.Schema;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableTable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoNamer {

  private static final ImmutableTable<Schema.Type, Schema.Format, String> TYPE_NAMES =
      new ImmutableTable.Builder<Schema.Type, Schema.Format, String>()
          .put(Schema.Type.ANY, Schema.Format.EMPTY, "interface{}")
          .put(Schema.Type.BOOLEAN, Schema.Format.EMPTY, "bool")
          .put(Schema.Type.INTEGER, Schema.Format.INT32, "int32")
          .put(Schema.Type.INTEGER, Schema.Format.UINT32, "uint32")
          .put(Schema.Type.NUMBER, Schema.Format.FLOAT, "float")
          .put(Schema.Type.NUMBER, Schema.Format.DOUBLE, "double")
          .put(Schema.Type.STRING, Schema.Format.EMPTY, "string")
          .put(Schema.Type.STRING, Schema.Format.BYTE, "[]byte")
          .put(Schema.Type.STRING, Schema.Format.DATE, "string") // TODO: Probably wrong.
          .put(Schema.Type.STRING, Schema.Format.DATETIME, "string")
          .put(Schema.Type.STRING, Schema.Format.INT64, "int64")
          .put(Schema.Type.STRING, Schema.Format.UINT64, "uint64")
          .build();

  private final Document document;

  public GoNamer(Document document) {
    this.document = document;
  }

  public String getServiceImportPath() {
    String name = document.name().toLowerCase();
    String version = document.version();

    Pattern p = Pattern.compile("^(.+)_(v[\\d\\.]+)$");
    Matcher m = p.matcher(version);
    if (version.equals("alpha") || version.equals("beta")) {
      version = "v0." + version;
    } else if (m.matches()) {
      version = m.group(1) + "/" + m.group(2);
    }
    return String.format("google.golang.org/api/%s/%s", name, version);
  }

  public String getServicePackageName() {
    String importPath = getServiceImportPath();
    String segments[] = importPath.split("/");
    return segments[segments.length - 2];
  }

  public String getServiceVarName() {
    return document.name().toLowerCase() + "Service";
  }

  public String getServiceTypeName() {
    return getServicePackageName() + ".Service";
  }

  public String getScopeConstName(String scope) {
    String prefix = "https://www.googleapis.com/auth/";
    if (!scope.startsWith(prefix)) {
      String https = "https://";
      return GoSymbol.from(scope.substring(https.length())).toUpperCamel().name() + "Scope";
    }
    return getServicePackageName()
        + "."
        + GoSymbol.from(scope.substring(prefix.length())).toUpperCamel().name()
        + "Scope";
  }

  public String getTypeName(Schema schema) {
    schema = schema.dereference();
    switch (schema.type()) {
      default:
        if (!schema.repeated()) {
          break;
        }
        return "[]" + getTypeName(schema.withRepeated(false));
      case ARRAY:
        return "[]" + getTypeName(schema.items());
      case OBJECT:
        if (schema.additionalProperties() != null) {
          return "map[string]" + getTypeName(schema.additionalProperties());
        }
        return getObjectTypeName(schema);
    }
    return TYPE_NAMES.get(schema.type(), schema.format());
  }

  private String getObjectTypeName(Schema schema) {
    StringBuilder typeNameBuilder = new StringBuilder(getServicePackageName() + ".");
    if (!Strings.isNullOrEmpty(schema.id())) {
      return typeNameBuilder.append(GoSymbol.from(schema.id()).toUpperCamel().name()).toString();
    }

    List<String> segments = Arrays.asList(schema.path().split("\\."));
    // TODO: Replace `segments.indexOf("schemas")` with 0?
    int i = segments.indexOf("schemas") + 1;

    boolean addItem = true;

    while (i < segments.size()) {
      typeNameBuilder.append(GoSymbol.from(segments.get(i)).toUpperCamel().name());
      while (i + 1 < segments.size() && segments.get(i + 1).equals("items")) {
        // TODO: Tests should verify this multiple "item" chain possibility.
        if (addItem) {
          typeNameBuilder.append("Item");
        }
        i++;
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("properties")) {
        i++;
      }
      if (i + 1 < segments.size() && segments.get(i + 1).equals("additionalProperties")) {
        i++;
      }
      addItem = false;
      i++;
    }
    return typeNameBuilder.toString();
  }

  public String getRequestBodyVarName() {
    return "rb";
  }

  public String getServiceRequestFuncName(String methodPath) {
    StringBuilder typeNameBuilder = new StringBuilder();
    List<String> methodIdSegments = Arrays.asList(methodPath.split("\\."));
    for (int i = 1; i < methodIdSegments.size(); i += 2) {
      GoSymbol symbol = GoSymbol.from(methodIdSegments.get(i));
      typeNameBuilder.append(symbol.toUpperCamel().name());
      if (i < methodIdSegments.size() - 1) {
        typeNameBuilder.append(".");
      }
    }
    return typeNameBuilder.toString();
  }

  public String getRequestVarName() {
    return "req";
  }

  public String getResponseVarName() {
    return "resp";
  }

  public String getRequestTypeName(Method method) {
    return getRequestTypeName(method.path());
  }

  private String getRequestTypeName(String methodPath) {
    List<String> methodIdSegments = Arrays.asList(methodPath.split("\\."));
    // `prefix` is the last resource name, or the empty string if the method is top-level.
    String prefix = "";
    int i = methodIdSegments.size() - 3;
    if (i > 0) {
      prefix = GoSymbol.from(methodIdSegments.get(i)).toUpperCamel().name();
    }
    i = methodIdSegments.size() - 1;
    String methodName = GoSymbol.from(methodIdSegments.get(i)).toUpperCamel().name();
    return getServicePackageName() + "." + prefix + methodName + "Call";
  }
}
