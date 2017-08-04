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
package com.google.api.codegen.discovery2.transformer.py;

import com.google.api.codegen.discovery2.transformer.NameUtil;
import com.google.api.codegen.discovery2.transformer.Symbol;
import com.google.auto.value.AutoValue;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import java.util.Set;

@AutoValue
public abstract class PythonSymbol implements Symbol {

  public static PythonSymbol from(String name) {
    return new AutoValue_PythonSymbol(name, KEYWORDS.contains(name), RESERVED.contains(name));
  }

  public PythonSymbol toLowerUnderscore() {
    String name =
        CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, NameUtil.upperCamel(name()));
    return new AutoValue_PythonSymbol(name, KEYWORDS.contains(name), RESERVED.contains(name));
  }

  public abstract String name();

  public abstract boolean isKeyword();

  public abstract boolean isReserved();

  private static final Set<String> KEYWORDS =
      ImmutableSet.<String>builder()
          .add(
              "False",
              "None",
              "True",
              "and",
              "as",
              "assert",
              "break",
              "class",
              "continue",
              "def",
              "del",
              "elif",
              "else",
              "except",
              "finally",
              "for",
              "from",
              "global",
              "if",
              "import",
              "in",
              "is",
              "lambda",
              "nonlocal",
              "not",
              "or",
              "pass",
              "raise",
              "return",
              "try",
              "while",
              "with",
              "yield",
              "body") // "body" is reserved by the client library.
          .build();

  private static final Set<String> RESERVED =
      ImmutableSet.<String>builder()
          .add(
              "and",
              "del",
              "from",
              "not",
              "while",
              "as",
              "elif",
              "global",
              "or",
              "with",
              "assert",
              "else",
              "if",
              "pass",
              "yield",
              "break",
              "except",
              "import",
              "print",
              "class",
              "exec",
              "in",
              "raise",
              "continue",
              "finally",
              "is",
              "return",
              "def",
              "for",
              "lambda",
              "try",
              // Built-ins
              "abs",
              "all",
              "any",
              "apply",
              "basestring",
              "bin",
              "bool",
              "buffer",
              "bytearray",
              "bytes",
              "callable",
              "chr",
              "classmethod",
              "cmp",
              "coerce",
              "compile",
              "complex",
              "copyright",
              "credits",
              "delattr",
              "dict",
              "dir",
              "divmod",
              "enumerate",
              "eval",
              "execfile",
              "exit",
              "file",
              "filter",
              "float",
              "format",
              "frozenset",
              "getattr",
              "globals",
              "hasattr",
              "hash",
              "help",
              "hex",
              "id",
              "input",
              "int",
              "intern",
              "isinstance",
              "issubclass",
              "iter",
              "len",
              "license",
              "list",
              "locals",
              "long",
              "map",
              "max",
              "memoryview",
              "min",
              "next",
              "object",
              "oct",
              "open",
              "ord",
              "pow",
              "print",
              "property",
              "quit",
              "range",
              "raw_input",
              "reduce",
              "reload",
              "repr",
              "reversed",
              "round",
              "set",
              "setattr",
              "slice",
              "sorted",
              "staticmethod",
              "str",
              "sum",
              "super",
              "tuple",
              "type",
              "unichr",
              "unicode",
              "vars",
              "xrange",
              "zip")
          .build();
}
