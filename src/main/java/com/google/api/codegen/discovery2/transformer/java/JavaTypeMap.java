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
import com.google.api.codegen.discovery2.transformer.TypeMap;
import com.google.api.codegen.discovery2.transformer.csharp.CSharpSymbol;
import com.google.common.collect.ImmutableTable;
import java.util.HashMap;
import java.util.Map;

public class JavaTypeMap implements TypeMap {

  private final JavaNamer namer;
  private final HashMap<String, String> importNames;

  public JavaTypeMap(Document document) {
    namer = new JavaNamer(document);
    importNames = new HashMap<>();
  }

  public void addImportName(String importName) {
    add(importName);
  }

  public String addRequest(Method method) {
    return add(namer.getRequestTypeName(method));
  }

  public String addService() {
    return add(namer.getServiceTypeName());
  }

  private String add(String typeName) {
    // ["com.google.api.services.foo.v1.model.Foo", "Bar$Baz"]
    String segments[] = typeName.split("\\$", 2);
    if (segments.length > 1) {
      segments[1] = segments[1].replace("$", ".");
    }

    String baseTypeName = segments[0];
    String baseClassName = baseTypeName.substring(baseTypeName.lastIndexOf(".") + 1);

    // If there's no package name, skip.
    // ex: "int"
    if (!typeName.contains(".")) {
      return baseTypeName;
    }
    // If the package name is "java.lang", skip.
    // ex: "java.lang.String"
    if (typeName.startsWith("java.lang")) {
      return baseClassName;
    }

    String fullTypeName = baseTypeName;
    String fullClassName = baseClassName;
    if (segments.length > 1) {
      fullTypeName += "." + segments[1];
      fullClassName += "." + segments[1];
    }

    if (importNames.containsKey(baseClassName)) {
      if (importNames.get(baseClassName).equals(baseTypeName)) {
        return fullClassName;
      } else {
        return fullTypeName;
      }
    } else {
      importNames.put(baseClassName, baseTypeName);
      return fullClassName;
    }
  }

  @Override
  public String add(Schema schema) {
    return add(namer.getType(schema));
  }

  private String add(JavaNamer.JavaType type) {
    StringBuilder typeNameBuilder = new StringBuilder(add(type.typeName()));

    if (!type.parameterTypes().isEmpty()) {
      typeNameBuilder.append("<");
      for (int i = 0; i < type.parameterTypes().size(); i++) {
        JavaNamer.JavaType parameterType = type.parameterTypes().get(i);
        typeNameBuilder.append(add(parameterType));
        if (i < type.parameterTypes().size() - 1) {
          typeNameBuilder.append(", ");
        }
      }
      typeNameBuilder.append(">");
    }
    return typeNameBuilder.toString();
  }

  public String getStructFieldName(Schema schema) {
    String segments[] = schema.path().split("\\.");
    // TODO: This isn't even close to matching what the client generator does, but it's good enough.
    return CSharpSymbol.from(segments[segments.length - 1]).toUpperCamel().name();
  }

  public String getClassPropertyName(Schema schema) {
    return "";
  }

  public String getGetterFuncName(Schema schema) {
    String segments[] = schema.path().split("\\.");
    return "get" + JavaSymbol.from(segments[segments.length - 1], true).toUpperCamel().name();
  }

  public String getSetterFuncName(Schema schema) {
    String segments[] = schema.path().split("\\.");
    return "set" + JavaSymbol.from(segments[segments.length - 1], true).toUpperCamel().name();
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
              return String.format(format, Long.valueOf(override).toString());
            case UINT64:
              String typeName = add("java.math.BigInteger");
              return String.format("new %s(\"%s\")", typeName, Long.valueOf(override).toString());
          }
          return String.format(format, override);
      }
    }
    JavaNamer.JavaType type = namer.getType(schema);
    switch (schema.type()) {
      default:
        if (!schema.repeated()) {
          break;
        }
      case ARRAY:
        type = JavaNamer.JavaType.from("java.util.ArrayList");
        // TODO: Note that we don't have to include the parameter types, since the compiler can infer them.
        return String.format("new %s<>()", add(type));
      case OBJECT:
        if (schema.additionalProperties() != null) {
          type = JavaNamer.JavaType.from("java.util.HashMap");
          return String.format("new %s<>()", add(type));
        }
        return String.format("new %s()", add(type));
    }
    if (schema.type() == Schema.Type.STRING && schema.format() == Schema.Format.UINT64) {
      return String.format("new %s(\"0\")", add("java.math.BigInteger"));
    }
    return ZEROS.get(schema.type(), schema.format());
  }

  public Map<String, String> getImportNames() {
    return new HashMap<>(importNames);
  }

  private static final ImmutableTable<Schema.Type, Schema.Format, String> ZEROS =
      new ImmutableTable.Builder<Schema.Type, Schema.Format, String>()
          .put(Schema.Type.ANY, Schema.Format.EMPTY, "new Object()")
          .put(Schema.Type.BOOLEAN, Schema.Format.EMPTY, "false")
          .put(Schema.Type.INTEGER, Schema.Format.INT32, "0")
          .put(Schema.Type.INTEGER, Schema.Format.UINT32, "0L")
          .put(Schema.Type.NUMBER, Schema.Format.FLOAT, "0.0f")
          .put(Schema.Type.NUMBER, Schema.Format.DOUBLE, "0.0d")
          .put(Schema.Type.STRING, Schema.Format.EMPTY, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.BYTE, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.DATE, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.DATETIME, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.INT64, "0L")
          .build();
}
