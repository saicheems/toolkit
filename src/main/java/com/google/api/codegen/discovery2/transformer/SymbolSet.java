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
package com.google.api.codegen.discovery2.transformer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;

public class SymbolSet {

  private static final Set<String> ACRONYMS = ImmutableSet.of("api", "http", "iam", "sql", "xml");

  private final Set<String> symbols;

  public SymbolSet() {
    symbols = new HashSet<>();
  }

  public String add(String name) {
    int suffix = 0;

    String uniqueName;
    do {
      uniqueName = name + (suffix > 1 ? suffix : "");
      suffix++;
    } while (symbols.contains(uniqueName));
    symbols.add(uniqueName);
    return uniqueName;
  }

  @Deprecated
  public String add(Symbol symbol) {
    // `symbol` is expected to start with a lowercase character.
    Preconditions.checkArgument(Character.isLowerCase(symbol.name().charAt(0)));
    // TODO: Override this function if you want special escape behavior of symbol names.
    int suffix = 1;

    // TODO: Handle different capitalizations? Boolean, for example.
    if (symbol.isReserved()) {
      suffix++;
    }

    // TODO: Reminder to take a second look at this.
    // Ensure names that begin with common acronyms have the correct capitalization.
    // ex: "sQLAdmin" -> "sqlAdmin"
    //
    // This is guaranteed to work in all cases because the symbol this function is called with is
    // expected to be either lower camel or lower underscore.
    String name = symbol.name();
    for (String acronym : ACRONYMS) {
      if (name.toLowerCase().startsWith(acronym)) {
        name = acronym + name.substring(acronym.length());
        break;
      }
    }

    String uniqueName;
    do {
      uniqueName = name + (suffix > 1 ? suffix : "");
      suffix++;
    } while (symbols.contains(uniqueName));
    symbols.add(uniqueName);
    return uniqueName;
  }
}
