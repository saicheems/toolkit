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
package com.google.api.codegen.discovery.transformer;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

public class SymbolSet {

  private final Set<String> symbols;

  public SymbolSet() {
    symbols = ImmutableSet.of();
  }

  public SymbolSet(Set<String> seed) {
    symbols = ImmutableSet.copyOf(seed);
  }

  public String uniqueSymbol(String symbol) {
    String uniqueSymbol = symbol;
    int suffix = 1;
    while (symbols.contains(uniqueSymbol)) {
      uniqueSymbol = String.format("%s%d", symbol, suffix);
    }
    return uniqueSymbol;
  }

  public SymbolSet withSymbol(String symbol) {
    return new SymbolSet(new ImmutableSet.Builder<String>().addAll(symbols).add(symbol).build());
  }
}
