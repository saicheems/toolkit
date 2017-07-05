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
import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.discovery2.transformer.TypeMap;
import com.google.common.collect.ImmutableTable;
import java.util.HashMap;

public class JavaTypeMap implements TypeMap {

  private final JavaNamer namer;
  private final HashMap<String, String> importNames;

  public JavaTypeMap(Document document) {
    this.namer = new JavaNamer(document);
    importNames = new HashMap<>();
  }

  @Override
  public String add(Schema schema) {
    Schema copy = schema;

    //    Set<String> delims = ImmutableSet.of("<", ">", ",", " ");
    //    StringTokenizer st = new StringTokenizer(namer.getTypeName(schema), Joiner.on("").join(delims), true);
    //
    //    StringBuilder typeNameBuilder = new StringBuilder("");
    //    while (st.hasMoreTokens()) {
    //      String token = st.nextToken();
    //      if (delims.contains(token)) {
    //        typeNameBuilder.append(token);
    //        continue;
    //      }
    //      if (importNames.containsKey(token)) {
    //        typeNameBuilder.append(importNames.get(token));
    //      } else {
    //
    //      }
    //    }

    return namer.getTypeName(schema);
  }

  public String getFieldName(Schema schema) {
    return "";
  }

  public String getClassPropertyName(Schema schema) {
    return "";
  }

  public String getValue(Schema schema, String override) {
    return "";
  }

  private static final ImmutableTable<Schema.Type, Schema.Format, String> ZEROS =
      new ImmutableTable.Builder<Schema.Type, Schema.Format, String>()
          .put(Schema.Type.ANY, Schema.Format.EMPTY, "new Object()")
          .put(Schema.Type.BOOLEAN, Schema.Format.EMPTY, "false")
          .put(Schema.Type.INTEGER, Schema.Format.INT32, "0")
          .put(Schema.Type.INTEGER, Schema.Format.UINT32, "0")
          .put(Schema.Type.NUMBER, Schema.Format.FLOAT, "0.0f")
          .put(Schema.Type.NUMBER, Schema.Format.DOUBLE, "0.0d")
          .put(Schema.Type.STRING, Schema.Format.EMPTY, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.BYTE, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.DATE, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.DATETIME, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.INT64, "0L")
          .put(Schema.Type.STRING, Schema.Format.UINT64, "new BigInteger(\"0\")")
          .build();
}
