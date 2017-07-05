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

import com.google.api.codegen.discovery2.transformer.NameUtil;
import com.google.api.codegen.discovery2.transformer.Symbol;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import java.util.Set;

@AutoValue
public abstract class JavaSymbol implements Symbol {

  public static JavaSymbol from(String name) {
    return new AutoValue_JavaSymbol(name, RESERVED.contains(name));
  }

  public JavaSymbol toLowerCamel() {
    return from(NameUtil.lowerCamel(name()));
  }

  public JavaSymbol toUpperCamel() {
    return from(NameUtil.upperCamel(name()));
  }

  public abstract String name();

  public abstract boolean isReserved();

  // All lowercase because in Java the code generator checks the lowercase version of a name to
  // decide if it is reserved.
  private static final Set<String> RESERVED =
      ImmutableSet.<String>builder()
          .add(
              "abstract",
              "assert",
              "boolean",
              "break",
              "byte",
              "case",
              "catch",
              "char",
              "class",
              "const",
              "continue",
              "default",
              "do",
              "double",
              "else",
              "enum",
              "extends",
              "final",
              "finally",
              "float",
              "for",
              "goto",
              "if",
              "implements",
              "import",
              "instanceof",
              "int",
              "interface",
              "long",
              "native",
              "new",
              "package",
              "private",
              "protected",
              "public",
              "return",
              "short",
              "static",
              "strictfp",
              "super",
              "switch",
              "synchronized",
              "this",
              "throw",
              "throws",
              "transient",
              "try",
              "void",
              "volatile",
              "while",
              "entry",
              "float",
              "integer",
              "object",
              "string",
              "true",
              "false",
              "null")
          .build();
}
