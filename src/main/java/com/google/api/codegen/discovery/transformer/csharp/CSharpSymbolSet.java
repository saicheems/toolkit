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
import com.google.api.codegen.discovery.transformer.SymbolSet;

public class CSharpSymbolSet extends SymbolSet {

  public String add(Symbol symbol) {
    // TODO: Note that this method converts to a lowercase symbol.
    String name = symbol.name();
    if (symbol.isReserved()) {
      name += "2";
    }
    return add(name);
  }
}
