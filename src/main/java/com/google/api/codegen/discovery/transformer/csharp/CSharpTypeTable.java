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

import com.google.common.collect.Sets;
import java.util.Set;

public class CSharpTypeTable {

  public static final Set<String> RESERVED_IDENTIFIERS =
      Sets.newHashSet(
          "abstract",
          "as",
          "base",
          "bool",
          "break",
          "byte",
          "case",
          "catch",
          "char",
          "checked",
          "class",
          "const",
          "continue",
          "decimal",
          "default",
          "delegate",
          "do",
          "double",
          "else",
          "enum",
          "event",
          "explicit",
          "extern",
          "false",
          "finally",
          "fixed",
          "float",
          "for",
          "foreach",
          "goto",
          "if",
          "implicit",
          "in",
          "int",
          "interface",
          "internal",
          "is",
          "lock",
          "long",
          "namespace",
          "new",
          "null",
          "object",
          "operator",
          "out",
          "override",
          "params",
          "private",
          "protected",
          "public",
          "readonly",
          "ref",
          "return",
          "sbyte",
          "sealed",
          "short",
          "sizeof",
          "stackalloc",
          "static",
          "string",
          "struct",
          "switch",
          "this",
          "throw",
          "true",
          "try",
          "typeof",
          "uint",
          "ulong",
          "unchecked",
          "unsafe",
          "ushort",
          "using",
          "virtual",
          "void",
          "volatile",
          "while",
          "async",
          "await");

  /*private final Document document;
  private final Method method;

  private final String rootNamespace;

  // TODO(saicheems): Organize the using directives more nicely?
  private final List<String> usingDirectives;
  private final List<String> usingAliasDirectives;

  public CSharpTypeTable(Document document, Method method) {
    this.document = document;
    this.method = method;

    String name = Names.camelCase(document.canonicalName());
    String version = Names.noDotsOrDashes(document.version());
    rootNamespace = String.format("Google.Apis.%s.%s", name, version);

    usingDirectives = new ArrayList<>();
    usingAliasDirectives = new ArrayList<>();
  }

  public String getAndSaveServiceTypeName() {
    // "Google.Foo.v1", "FooService"
    String typeName = Names.camelCase(document.canonicalName()) + "Service";
    addUsingDirective(rootNamespace);
    return typeName;
  }

  public String getAndSaveRequestTypeName() {
    String typeName = "";
    String resources[] = method.id().split("\\.");
    for (int i = 1; i < resources.length - 1; i++) {
      typeName += Names.camelCase(resources[i]) + "Resource.";
    }
    typeName += Names.camelCase(resources[resources.length - 1]) + "Request";
    addUsingDirective(rootNamespace);
    return typeName;
  }

  public String getAndSaveSchemaTypeName(Schema schema) {
    Preconditions.checkArgument(schema.type() == Schema.Type.OBJECT);
    String typeName = "Data." + Names.camelCase(schema.id());
    addUsingAliasDirective(rootNamespace + ".Data", "Data");
    return typeName;
  }

  public String getAndSaveEnumTypeName(String fieldName) {
    // only method parameters can be typed enums.
    // regular schema enums are treated as strings.
    // No need to add to the type table because it shares the same rootNamespace as the request type.
    return getAndSaveRequestTypeName() + Names.camelCase(fieldName) + "Enum";
  }

  public void addUsingDirective(String namespace) {
    usingDirectives.add(String.format("using %s", namespace));
  }

  public void addUsingAliasDirective(String namespace, String alias) {
    usingAliasDirectives.add(String.format("using %s = %s", namespace, alias));
  }

  public List<String> getUsingDirectives() {
    return new ArrayList<>(usingDirectives);
  }

  public List<String> getUsingAliasDirectives() {
    return new ArrayList<>(usingAliasDirectives);
  }
  */
}
