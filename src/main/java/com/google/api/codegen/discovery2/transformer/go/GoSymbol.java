/* Copyright 2016 Google Inc
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
package com.google.api.codegen.discovery2.transformer.go;

import com.google.api.codegen.discovery2.transformer.Symbol;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import java.util.Set;

@AutoValue
public abstract class GoSymbol implements Symbol {

  public static GoSymbol from(String name) {
    return new AutoValue_GoSymbol(name, RESERVED.contains(name));
  }

  public GoSymbol toLowerCamel() {
    String name = toUpperCamel().name();
    return new AutoValue_GoSymbol(
        name.substring(0, 1).toLowerCase() + name.substring(1), RESERVED.contains(name));
  }

  public GoSymbol toUpperCamel() {
    StringBuilder sb = new StringBuilder();
    boolean preserveUnderscore = false;
    boolean capitalize = true;
    for (int i = 0; i < name().length(); i++) {
      char c = name().charAt(i);
      if (c == '_') {
        if (preserveUnderscore || name().substring(i).startsWith("__")) {
          preserveUnderscore = true;
        } else {
          capitalize = true;
          continue;
        }
      } else {
        preserveUnderscore = false;
      }
      if (c == '-' || c == '.' || c == '$' || c == '/') {
        capitalize = true;
        continue;
      }
      if (capitalize) {
        c = Character.toUpperCase(c);
        capitalize = false;
      }
      sb.append(c);
    }
    String name = sb.toString();
    return new AutoValue_GoSymbol(name, RESERVED.contains(name));
  }

  public abstract String name();

  public abstract boolean isReserved();

  private static final Set<String> RESERVED =
      ImmutableSet.<String>builder()
          .add(
              "break",
              "default",
              "func",
              "interface",
              "select",
              "case",
              "defer",
              "go",
              "map",
              "struct",
              "chan",
              "else",
              "goto",
              "package",
              "switch",
              "const",
              "fallthrough",
              "if",
              "range",
              "type",
              "continue",
              "for",
              "import",
              "return",
              "var")
          .build();
}
