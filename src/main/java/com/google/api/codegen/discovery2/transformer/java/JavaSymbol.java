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

  public static JavaSymbol from(String name, boolean ignoreCase) {
    String lowerCaseName = name.toLowerCase();
    boolean reserved = RESERVED.contains(ignoreCase ? lowerCaseName : name);
    if (ignoreCase) {
      for (String s : UPPER_CAMEL_RESERVED) {
        if (lowerCaseName.equals(s.toLowerCase())) {
          reserved = true;
          break;
        }
      }
    } else {
      reserved |= UPPER_CAMEL_RESERVED.contains(name);
    }
    return new AutoValue_JavaSymbol(name, false, reserved);
  }

  public JavaSymbol toLowerCamel() {
    return from(NameUtil.lowerCamel(name()), ignoreCase());
  }

  public JavaSymbol toUpperCamel() {
    return from(NameUtil.upperCamel(name()), ignoreCase());
  }

  public abstract String name();

  public abstract boolean ignoreCase();

  public abstract boolean isReserved();

  private static final Set<String> UPPER_CAMEL_RESERVED =
      ImmutableSet.<String>builder().add("Entry", "Float", "Integer", "Object", "String").build();

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
              "true",
              "false",
              "null",
              "Entry",
              "Float",
              "Integer",
              "Object",
              "String")
          .build();
}
