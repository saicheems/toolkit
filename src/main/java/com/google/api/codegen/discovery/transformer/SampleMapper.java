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

import com.google.api.codegen.discovery.Schema;

// contains methods to map discovery properties onto a language sample
public abstract class SampleMapper {

  public String getNamespaceName() {
    return "";
  }

  public String getClassName() {
    return "";
  }

  public String getObjectPropertyName(Schema schema) {
    return "";
  }

  public abstract String getTypeName(Schema schema);

  public abstract String getZeroValue(Schema schema);
}
