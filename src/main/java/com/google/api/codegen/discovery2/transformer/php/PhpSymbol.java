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
package com.google.api.codegen.discovery2.transformer.php;

import com.google.api.codegen.discovery2.transformer.NameUtil;
import com.google.api.codegen.discovery2.transformer.Symbol;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import java.util.Set;

@AutoValue
public abstract class PhpSymbol implements Symbol {

  public static PhpSymbol from(String name, boolean ignoreCase) {
    String lowerCaseName = name.toLowerCase();
    return new AutoValue_PhpSymbol(
        name, ignoreCase, RESERVED.contains(ignoreCase ? lowerCaseName : name));
  }

  public PhpSymbol toLowerCamel() {
    return from(NameUtil.lowerCamel(name()), ignoreCase());
  }

  public PhpSymbol toUpperCamel() {
    return from(NameUtil.upperCamel(name()), ignoreCase());
  }

  public abstract String name();

  public abstract boolean ignoreCase();

  public abstract boolean isReserved();

  private static final Set<String> RESERVED =
      ImmutableSet.<String>builder()
          .add(
              "abstract",
              "and",
              "array",
              "as",
              "break",
              "call",
              "callable",
              "case",
              "catch",
              "cfunction",
              "class",
              "clone",
              "const",
              "continue",
              "declare",
              "default",
              "do",
              "else",
              "elseif",
              "empty",
              "enddeclare",
              "endfor",
              "endforeach",
              "endif",
              "endswitch",
              "endwhile",
              "extends",
              "final",
              "finally",
              "for",
              "foreach",
              "function",
              "global",
              "goto",
              "if",
              "implements",
              "interface",
              "instanceof",
              "list",
              "namespace",
              "new",
              "old_function",
              "or",
              "parent",
              "private",
              "protected",
              "public",
              "return",
              "static",
              "switch",
              "throw",
              "trait",
              "try",
              "unset",
              "use",
              "var",
              "while",
              "xor",
              "yield",
              "bool",
              "boolean",
              "int",
              "integer",
              "file",
              "float",
              "double",
              "string",
              "array",
              "object",
              "null",
              "resource")
          .build();
}
