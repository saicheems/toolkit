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
package com.google.api.codegen.discovery2.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.codegen.discovery.DefaultString;
import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.discovery2.viewmodel.FieldView;
import com.google.common.base.CaseFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FieldTransformer {

  //  // Returns a field converted from schema.
  //  // Only the discoveryFieldName, isList, and isMap properties are assigned.
  //  public static FieldView transform(Schema schema, TypeMap typeMap, SymbolSet symbolSet) {
  //    FieldView fieldView = transform(schema, typeMap);
  //    return fieldView.withVarName(symbolSet.add(fieldView.discoveryFieldName()));
  //  }

  public static FieldView transform(
      Schema schema, SymbolSet symbolSet, TypeMap typeMap, JsonNode override) {
    // The name of the field this schema is assigned to is the last element of its path.
    return transform(schema, symbolSet, typeMap, override, "");
  }

  public static FieldView transform(
      Schema schema, SymbolSet symbolSet, TypeMap typeMap, JsonNode override, String varName) {
    int i = schema.path().lastIndexOf('.');
    String discoveryFieldName = schema.path().substring(i + 1);
    if (varName.isEmpty()) {
      varName = discoveryFieldName;
    }
    if (symbolSet != null) {
      varName = symbolSet.add(varName);
    }

    String value = "";

    List<FieldView> fields = new ArrayList<>();

    if (schema.type() == Schema.Type.STRING
        && schema.format() == Schema.Format.EMPTY
        && !schema.isEnum()) {
      String format = "my-%s";
      if (schema.location().equals("path")) {
        value = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, discoveryFieldName);
        value = String.format(format, value.replaceAll("_", "-"));
      }
      String defaultString = DefaultString.getNonTrivialPlaceholder(schema.pattern(), format);
      if (!defaultString.isEmpty()) {
        value = defaultString;
      }
    }

    String typeName;
    value = typeMap.getValue(schema, value);

    if (schema.type() == Schema.Type.OBJECT && schema.additionalProperties() != null) {
      //typeMap.add(schema); // So "System.Collections.Generic" is imported.
      typeName = typeMap.add(schema.additionalProperties().dereference());
    } else if (schema.type() == Schema.Type.ARRAY) {
      typeName = typeMap.add(schema.items().dereference());
    } else {
      typeName = typeMap.add(schema);
      if (override == null && schema.repeated()) {
        // "item" is added to the symbol table in the recursive call to `transform`.
        fields.add(
            transform(schema.withRepeated(false), symbolSet, typeMap, null, "item")
                .withDescription(""));
      }
    }

    // TODO: Figure out overrides situation!
    if (override != null) {
      switch (override.getNodeType()) {
        default:
          if (!schema.repeated()) {
            throw new IllegalStateException(
                String.format("unsupported node type: %s", override.getNodeType().name()));
          }
          // Fall-through if the schema is repeated.
        case ARRAY:
          for (JsonNode node : override) {
            Schema items = schema.repeated() ? schema.withRepeated(false) : schema.items();
            fields.add(
                transform(items.dereference(), symbolSet, typeMap, node, "item")
                    .withDescription(""));
          }
          break;
        case OBJECT:
          Iterator<Map.Entry<String, JsonNode>> objectFields = override.fields();

          while (objectFields.hasNext()) {
            Map.Entry<String, JsonNode> entry = objectFields.next();
            if (schema.additionalProperties() != null) {
              // It's a map!
              fields.add(
                  transform(schema.additionalProperties(), symbolSet, typeMap, entry.getValue())
                      .withDescription("")
                      .withKeyValue(entry.getKey()));
            } else {
              Schema propertySchema = schema.properties().get(entry.getKey());
              fields.add(transform(propertySchema, symbolSet, typeMap, entry.getValue()));
            }
          }
          break;
        case BOOLEAN:
        case NUMBER:
        case STRING:
          value = typeMap.getValue(schema, override.asText());
          break;
      }
    }

    return FieldView.newBuilder()
        .classPropertyName(typeMap.getClassPropertyName(schema))
        .description(schema.description())
        .discoveryFieldName(discoveryFieldName)
        .fieldName(typeMap.getFieldName(schema))
        .fields(fields)
        .getterFuncName(typeMap.getGetterFuncName(schema))
        .isArray(schema.type() == Schema.Type.ARRAY || schema.repeated())
        .isMap(schema.type() == Schema.Type.OBJECT && schema.additionalProperties() != null)
        .keyValue("")
        .required(schema.required())
        .setterFuncName(typeMap.getSetterFuncName(schema))
        .typeName(typeName)
        .value(value)
        .varName(varName)
        .build();
  }
}
