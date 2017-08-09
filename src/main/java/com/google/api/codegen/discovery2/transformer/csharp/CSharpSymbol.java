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
package com.google.api.codegen.discovery2.transformer.csharp;

import com.google.api.codegen.discovery2.transformer.NameUtil;
import com.google.api.codegen.discovery2.transformer.Symbol;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
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
    return from(NameUtil.lowerCamel(name()));
  }

  public CSharpSymbol toUpperCamel() {
    return from(NameUtil.upperCamel(name()));
  }

  public abstract String name();

  public abstract boolean isReserved();

  private static final Set<String> RESERVED =
      ImmutableSet.<String>builder()
          .add(
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
              "await")
          .build();
}