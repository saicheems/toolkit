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
package com.google.api.codegen.discovery.transformer;

import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.discovery.viewmodel.FieldView;

public class FieldTransformer {

  // Returns a field converted from schema.
  // Only the discoveryFieldName, isList, and isMap properties are assigned.
  public static FieldView transform(Schema schema, TypeMap typeMap, SymbolSet symbolSet) {
    FieldView fieldView = transform(schema, typeMap);
    return fieldView.withVarName(symbolSet.add(fieldView.discoveryFieldName()));
  }

  public static FieldView transform(Schema schema, TypeMap typeMap) {
    // The name of the field this schema is assigned to is the last element of its path.
    int i = schema.path().lastIndexOf('.');
    String discoveryFieldName = schema.path().substring(i + 1);

    return FieldView.newBuilder()
        .classPropertyName(typeMap.getClassPropertyName(schema))
        .discoveryFieldName(discoveryFieldName)
        .fieldName(typeMap.getFieldName(schema))
        .isList(schema.type() == Schema.Type.ARRAY || schema.repeated())
        .isMap(schema.type() == Schema.Type.OBJECT && schema.additionalProperties() != null)
        .typeName(typeMap.add(schema))
        .value(typeMap.getZero(schema))
        .varName("")
        .build();
  }
}
