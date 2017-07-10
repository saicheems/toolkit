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
import com.google.api.codegen.discovery2.transformer.TypeMap;
import com.google.common.collect.ImmutableTable;

public class PhpTypeMap implements TypeMap {

  private final PhpNamer namer;

  public PhpTypeMap(Document document) {
    namer = new PhpNamer(document);
  }

  public String add(Schema schema) {
    return namer.getTypeName(schema);
  }

  public String addService() {
    return namer.getServiceTypeName();
  }

  public String getClassPropertyName(Schema schema) {
    return "";
  }

  public String getGetterFuncName(Schema schema) {
    String segments[] = schema.path().split("\\.");
    return "get" + PhpSymbol.from(segments[segments.length - 1], true).toUpperCamel().name();
  }

  public String getSetterFuncName(Schema schema) {
    String segments[] = schema.path().split("\\.");
    return "set" + PhpSymbol.from(segments[segments.length - 1], true).toUpperCamel().name();
  }

  public String getFieldName(Schema schema) {
    return "";
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
          String format = "\'%s\'";
          switch (schema.format()) {
            case INT64:
            case UINT64:
              return String.format(format, Long.valueOf(override).toString());
              // Return a regular string for "BYTES", "DATE", and "DATETIME".
          }
          return String.format(format, override);
      }
    }
    switch (schema.type()) {
      default:
        if (schema.repeated()) {
          return ZEROS.get(Schema.Type.ARRAY, Schema.Format.EMPTY);
        }
        break;
      case OBJECT:
        if (schema.additionalProperties() != null) {
          return "array()";
        }
        return String.format("new %s()", add(schema));
    }
    return ZEROS.get(schema.type(), schema.format());
  }

  private static final ImmutableTable<Schema.Type, Schema.Format, String> ZEROS =
      new ImmutableTable.Builder<Schema.Type, Schema.Format, String>()
          .put(Schema.Type.ANY, Schema.Format.EMPTY, "new stdClass()")
          .put(Schema.Type.ARRAY, Schema.Format.EMPTY, "array()")
          .put(Schema.Type.BOOLEAN, Schema.Format.EMPTY, "false")
          .put(Schema.Type.INTEGER, Schema.Format.INT32, "0")
          .put(Schema.Type.INTEGER, Schema.Format.UINT32, "0")
          .put(Schema.Type.NUMBER, Schema.Format.FLOAT, "0.0")
          .put(Schema.Type.NUMBER, Schema.Format.DOUBLE, "0.0")
          .put(Schema.Type.STRING, Schema.Format.EMPTY, "''")
          .put(Schema.Type.STRING, Schema.Format.BYTE, "''")
          .put(Schema.Type.STRING, Schema.Format.DATE, "''")
          .put(Schema.Type.STRING, Schema.Format.DATETIME, "''")
          .put(Schema.Type.STRING, Schema.Format.INT64, "'0'")
          .put(Schema.Type.STRING, Schema.Format.UINT64, "'0'")
          .build();
}
