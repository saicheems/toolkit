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

import com.google.api.codegen.discovery.transformer.Symbol;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Sets;
import java.util.Set;

@AutoValue
public abstract class CSharpSymbol implements Symbol {

  public static CSharpSymbol from(String name) {
    // TODO: Note, this class only approximates how the actual C# generator works. For example "cl ass" is reserved here, but the C# generator will generate the class name "Class".
    // TODO: This implementation of reserved should be correct, for example:
    // "cl ass" ->  ("class", true) | ("Class", false)
    // "cl-ass" -> ("clAss", false) | ("ClAss", false)
    // "Class" ->   ("class", true) | ("Class", false)
    // "class" ->   ("class", true) | ("Class", false)
    return new AutoValue_CSharpSymbol(name, RESERVED.contains(name));
  }

  // TODO: Document that this class returns a lowerCamel string by default.
  public CSharpSymbol toLowerCamel() {
    String name = toUpperCamel().name(); // "foo-bar" -> "FooBar"
    name = name.substring(0, 1).toLowerCase() + name.substring(1);
    return from(name);
  }

  public CSharpSymbol toUpperCamel() {
    String name = name().replaceAll("\\s", "");
    String pieces[] = name.split("[\\./-]+");
    name = "";
    for (String p : pieces) {
      if (p.isEmpty()) {
        continue;
      }
      name += p.substring(0, 1).toUpperCase() + p.substring(1);
    }
    return from(name);
  }

  public abstract String name();

  public abstract boolean isReserved();

  private static final Set<String> RESERVED =
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
}
