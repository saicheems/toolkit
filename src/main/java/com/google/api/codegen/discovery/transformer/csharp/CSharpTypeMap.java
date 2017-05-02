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
package com.google.api.codegen.discovery.transformer.csharp;

import com.google.api.codegen.discovery.Document;
import com.google.api.codegen.discovery.Method;
import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.discovery.transformer.Path;
import com.google.api.codegen.discovery.transformer.TypeMap;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import java.util.HashMap;
import java.util.Map;

public class CSharpTypeMap implements TypeMap {

  private final CSharpNamer namer;
  private final HashMap<String, String> namespaceNames;

  private static final String SYSTEM_COLLECTIONS_GENERIC = "System.Collections.Generic";

  CSharpTypeMap(Document document) {
    this.namer = new CSharpNamer(document);
    namespaceNames = new HashMap<>();
  }

  @Override
  public String add(Schema schema) {
    Schema copy = schema;
    // Loops over the schema to collect namespaceNames.
    while (true) {
      Preconditions.checkNotNull(copy, "invalid schema");
      switch (copy.type()) {
        case ARRAY:
          namespaceNames.put(SYSTEM_COLLECTIONS_GENERIC, "");
          copy = copy.items();
          continue;
        case OBJECT:
          if (copy.additionalProperties() != null) { // Map
            namespaceNames.put(SYSTEM_COLLECTIONS_GENERIC, "");
            copy = copy.additionalProperties();
            continue;
          }
          namespaceNames.put(namer.getServiceNamespaceName() + ".Data", "Data");
          break;
      }
      if (schema.repeated()) {
        namespaceNames.put(SYSTEM_COLLECTIONS_GENERIC, "");
      }
      break;
    }
    if (schema.type() == Schema.Type.STRING && isTopLevelParameter(schema.path())) {
      // TODO: Actually a special enum.
      namespaceNames.put(namer.getServiceNamespaceName(), "");
    }
    return namer.getTypeName(schema);
  }

  String addService() {
    namespaceNames.put(namer.getServiceNamespaceName(), "");
    return namer.getServiceTypeName();
  }

  // Type name of the request, note this is different than the request body.
  String addRequest(Method method) {
    namespaceNames.put(namer.getServiceNamespaceName(), "");
    return namer.getRequestTypeName(method);
  }

  void addUsingDirective(String namespaceName) {
    namespaceNames.put(namespaceName, "");
  }

  public String getZero(Schema schema) {
    switch (schema.type()) {
      default:
        if (!schema.repeated()) {
          break;
        }
        // Fall through if the schema is repeated.
      case ARRAY:
      case OBJECT:
        return String.format("new %s()", namer.getTypeName(schema));
    }
    if (schema.type() == Schema.Type.STRING && isTopLevelParameter(schema.path())) {
      // TODO: Actually a special enum.
      return String.format("(%s) 0", namer.getTypeName(schema));
    }
    // TODO: Validate that the table returns a non-null value? It shouldn't be possible...
    return zeros.get(schema.type(), schema.format());
  }

  public String getClassPropertyName(Schema schema) {
    // TODO: This isn't even close to matching what the client generator does, but it's good enough for now.
    return CSharpSymbol.from(Path.from(schema.path()).lastSegment()).toUpperCamel().name();
  }

  public String getFieldName(Schema schema) {
    return "";
  }

  Map<String, String> namespaceNames() {
    return new HashMap<>(namespaceNames);
  }

  private static final ImmutableTable<Schema.Type, Schema.Format, String> zeros =
      new ImmutableTable.Builder<Schema.Type, Schema.Format, String>()
          .put(Schema.Type.ANY, Schema.Format.EMPTY, "new object()")
          .put(Schema.Type.BOOLEAN, Schema.Format.EMPTY, "false")
          .put(Schema.Type.INTEGER, Schema.Format.INT32, "0")
          .put(Schema.Type.INTEGER, Schema.Format.UINT32, "0")
          .put(Schema.Type.NUMBER, Schema.Format.FLOAT, "0.0f")
          .put(Schema.Type.NUMBER, Schema.Format.DOUBLE, "0.0")
          .put(Schema.Type.STRING, Schema.Format.EMPTY, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.BYTE, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.DATE, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.DATETIME, "\"\"")
          .put(Schema.Type.STRING, Schema.Format.INT64, "0L")
          .put(Schema.Type.STRING, Schema.Format.UINT64, "0L")
          .build();
}
