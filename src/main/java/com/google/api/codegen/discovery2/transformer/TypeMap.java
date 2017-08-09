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

import com.google.api.codegen.discovery.Schema;

public interface TypeMap {

  // Returns the short type name of the given schema and updates the type map.
  String add(Schema schema);

  String getClassPropertyName(Schema schema);

  String getGetterFuncName(Schema schema);

  String getSetterFuncName(Schema schema);

  String getStructFieldName(Schema schema);

  String getValue(Schema schema, String override);
}