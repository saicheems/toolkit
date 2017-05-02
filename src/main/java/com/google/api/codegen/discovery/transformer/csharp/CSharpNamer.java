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

import com.google.api.codegen.discovery.transformer.Path;
import java.util.List;

public class CSharpNamer {

  // TODO: Move anything related to names here, the getZero value stuff can stay in the type map.

  private final String canonicalName;

  public CSharpNamer(String canonicalName) {
    this.canonicalName = canonicalName;
  }

  public String getServiceTypeFullNameWithoutNamespace() {
    CSharpSymbol symbol = CSharpSymbol.from(canonicalName);
    String serviceClassName = symbol.toUpperCamel().name();
    if (symbol.isReserved()) {
      serviceClassName = serviceClassName + serviceClassName;
    }
    return serviceClassName + "Service";
  }

  public String getObjectTypeFullNameWithoutNamespace(String id, String schemaPath) {
    // root level schemas aren't escaped if they conflict with a reserved identifier (it's basically impossible though, they're all uppercase)

    // if id is present, the name is uppercase escaped id
    // top level array schemas are suffixed with "Items"
    // nested schemas with no id, or which conflict with their parent schema are their property name in uppercase suffixed with "Data"
    // nested schemas that conflict with their parent are again suffixed with "Schema"
    // nested map objects without a name are suffixed with "DataElement"

    // do nested objects ever have ids? or will they always be references?

    // If there's no ID, we use the key of the schema as the ID.
    // {
    //   "properties": { "mySchema": { ... } }
    //                   ^^^^^^^^^^
    // }
    if (!id.isEmpty()) {
      return CSharpSymbol.from(id).toUpperCamel().name();
    }
    Path path = Path.from(schemaPath);

    // We're making the potentially erroneous assumption that sub-schemas do not have IDs.
    // TODO: Note that there is a test for a sub-schema with in an ID in the C# generator, so there is a defined behavior for this case. However, no Discovery doc currently contains a sub-schema with an ID. I don't really see how it's possible, so I'm ignoring the case. In the exceedingly rare case that this becomes an issue, this function will have to take a Document as an argument to grab the actual Schema object for each segment.

    // TODO: Note that we're also making the assumption that the ID of the root schema is the same as its "id" field. It's currently true for all Discovery docs, so I believe it's a safe assumption. In the exceedingly rare case that this assumption is broken, the solution is the same as above.

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

  public String getRequestTypeFullNameWithoutNamespace(String methodPath) {
    Path path = Path.from(methodPath);
    String typeName = "";
    for (int i = 0; i < path.methodIdSegments().size(); i++) {
      typeName += getSafeClassName(path.methodIdSegments().get(i));
      if (i == path.methodIdSegments().size() - 1) {
        typeName += "Request";
      } else {
        typeName += "Resource.";
      }
    }
    return typeName;
  }

  public String getTopLevelEnumParameterTypeFullNameWithoutNamespace(String schemaPath) {
    String typeName = getSafeClassName(Path.from(schemaPath).lastSegment());
    return getRequestTypeFullNameWithoutNamespace(schemaPath) + "." + typeName + "Enum";
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
      typeName += CSharpSymbol.from(canonicalName).toUpperCamel().name();
    }
    return typeName + symbol.toUpperCamel().name();
  }
}
