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

import java.util.HashSet;
import java.util.Set;

public abstract class SymbolSet {

  private final Set<String> symbols;

  public SymbolSet() {
    symbols = new HashSet<>();
  }

  public abstract String add(String name);

  public String add(Symbol symbol) {
    // TODO: Override this function if you want special escape behavior of symbol names.
    int suffix = 1;
    if (symbol.isReserved()) {
      suffix++;
    }
    String uniqueName;
    do {
      uniqueName = symbol.name() + (suffix > 1 ? suffix : "");
      suffix++;
    } while (symbols.contains(uniqueName));
    symbols.add(uniqueName);
    return uniqueName;
  }
}
