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
package com.google.api.codegen.discovery.viewmodel;

import com.google.auto.value.AutoValue;

// represents fields in a query string or request body fields
@AutoValue
public abstract class FieldView {

  public static Builder newBuilder() {
    return new AutoValue_FieldView.Builder();
  }

  // For C# request optional fields.
  public abstract String classPropertyName();

  public abstract String discoveryFieldName();

  public static FieldView empty() {
    return newBuilder()
        .classPropertyName("")
        .discoveryFieldName("")
        .fieldName("")
        .isList(false)
        .isMap(false)
        .typeName("")
        .value("")
        .varName("")
        .build();
  }

  public abstract String fieldName();

  public abstract boolean isList();

  public abstract boolean isMap();

  public abstract Builder toBuilder();

  public abstract String typeName();

  public abstract String value();

  public abstract String varName();

  public FieldView withTypeName(String typeName) {
    return toBuilder().typeName(typeName).build();
  }

  public FieldView withVarName(String varName) {
    return toBuilder().varName(varName).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract FieldView build();

    public abstract Builder classPropertyName(String val);

    public abstract Builder discoveryFieldName(String val);

    public abstract Builder fieldName(String val);

    public abstract Builder isList(boolean val);

    public abstract Builder isMap(boolean val);

    public abstract Builder typeName(String val);

    public abstract Builder value(String val);

    public abstract Builder varName(String val);
  }

  // typedVariable (type, name, value)
  // - C#, Java

  // untypedVariable (name, value)
  // - Go, JavaScript, PHP, Ruby

  // dictionaryItem (key, value)
  // - Python

  // objectProperty (name, value)
  // JavaScript, Node.js
}
