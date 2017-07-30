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
import com.google.api.codegen.discovery2.transformer.TypeMap;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GoTypeMap implements TypeMap {

  private final GoNamer namer;
  private final HashMap<String, String> importPaths;
  private final Set<String> thirdPartyImportPaths;

  public GoTypeMap(Document document) {
    namer = new GoNamer(document);
    importPaths = new HashMap<>();
    thirdPartyImportPaths = new HashSet<>();
  }

  public boolean isThirdPartyImportPath(String importPath) {
    return thirdPartyImportPaths.contains(importPath);
  }

  public HashMap<String, String> getImportPaths() {
    return new HashMap<>(importPaths);
  }

  public String addImportPath(String importPath, boolean thirdParty) {
    String segments[] = importPath.split("/");
    String packageName = segments[segments.length - 1];
    importPaths.put(packageName, importPath);
    if (thirdParty) {
      thirdPartyImportPaths.add(importPath);
    }
    return packageName;
  }

  public String addService() {
    String packageName = namer.getServicePackageName();
    String importPath = namer.getServiceImportPath();
    importPaths.put(packageName, importPath);
    thirdPartyImportPaths.add(importPath);
    return packageName + ".Service";
  }

  public String addScopeConst(String scope) {
    addService();
    return namer.getScopeConstName(scope);
  }

  public String addRequest(Method method) {
    addService();
    return namer.getRequestTypeName(method);
  }

  public String add(Schema schema) {
    Schema copy = schema;
    while (true) {
      Preconditions.checkNotNull(copy, "invalid schema");
      switch (copy.type()) {
        case ARRAY:
          copy = copy.items();
          continue;
        case OBJECT:
          if (copy.additionalProperties() != null) {
            copy = copy.additionalProperties();
            continue;
          }
          addService();
          break;
      }
      break;
    }
    return namer.getTypeName(schema);
  }

  public String getClassPropertyName(Schema schema) {
    return "";
  }

  public String getGetterFuncName(Schema schema) {
    return "";
  }

  public String getSetterFuncName(Schema schema) {
    return "";
  }

  public String getStructFieldName(Schema schema) {
    String segments[] = schema.path().split("\\.");
    // TODO: This isn't even close to matching what the client generator does, but it's good enough.
    return GoSymbol.from(segments[segments.length - 1]).toUpperCamel().name();
  }

  public String getValue(Schema schema, String override) {
    if (!override.isEmpty()) {
      switch (schema.type()) {
        case BOOLEAN:
          return Boolean.valueOf(override).toString();
        case INTEGER:
          if (schema.format() == Schema.Format.UINT32) {
            return Long.valueOf(override).toString();
          }
          return Integer.valueOf(override).toString();
        case NUMBER:
          return Double.valueOf(override).toString();
        case STRING:
          String format = "\"%s\"";
          switch (schema.format()) {
            case INT64:
              return Long.valueOf(override).toString();
            case UINT64:
              return new BigInteger(override).toString();
              // Return a regular string for "BYTES", "DATE", and "DATETIME".
          }
          return String.format(format, override);
      }
    }
    switch (schema.type()) {
      default:
        if (!schema.repeated()) {
          break;
        }
        // Fall-through if the schema is repeated.
      case ARRAY:
        return add(schema) + "{}";
      case OBJECT:
        if (schema.additionalProperties() != null) {
          return "make(" + add(schema) + ")";
        }
        return "&" + add(schema);
    }
    // TODO: Validate that the table returns a non-null value? It shouldn't be possible...
    return ZEROS.get(schema.type(), schema.format());
  }

  private static final ImmutableTable<Schema.Type, Schema.Format, String> ZEROS =
      new ImmutableTable.Builder<Schema.Type, Schema.Format, String>()
          // TODO: ANY
          .put(Schema.Type.ARRAY, Schema.Format.EMPTY, "interface{}")
          .put(Schema.Type.BOOLEAN, Schema.Format.EMPTY, "false")
          .put(Schema.Type.INTEGER, Schema.Format.INT32, "int64(0)")
          .put(Schema.Type.INTEGER, Schema.Format.UINT32, "int64(0)")
          .put(Schema.Type.NUMBER, Schema.Format.FLOAT, "0.0")
          .put(Schema.Type.NUMBER, Schema.Format.DOUBLE, "0.0")
          .put(Schema.Type.STRING, Schema.Format.EMPTY, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.BYTE, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.DATE, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.DATETIME, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.INT64, "int64(0)")
          .put(Schema.Type.STRING, Schema.Format.UINT64, "uint64(0)")
          .build();
}
