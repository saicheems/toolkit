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
package com.google.api.codegen.discovery2.viewmodel;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.List;

// represents fields in a query string or request body fields
@AutoValue
public abstract class FieldView {

  public static Builder newBuilder() {
    return new AutoValue_FieldView.Builder();
  }

  public abstract String classPropertyName();

  public abstract String description();

  public abstract String discoveryFieldName();

  public static FieldView empty() {
    return newBuilder()
        .classPropertyName("")
        .description("")
        .discoveryFieldName("")
        .fieldName("")
        .fields(new ArrayList<FieldView>())
        .getterFuncName("")
        .isArray(false)
        .isMap(false)
        .keyValue("")
        .required(false)
        .setterFuncName("")
        .typeName("")
        .value("")
        .varName("")
        .build();
  }

  public abstract String fieldName();

  public abstract List<FieldView> fields();

  public abstract String getterFuncName();

  public abstract boolean isArray();

  public abstract boolean isMap();

  public abstract String keyValue();

  public abstract boolean required();

  public abstract String setterFuncName();

  public abstract Builder toBuilder();

  public abstract String typeName();

  public abstract String value();

  public abstract String varName();

  public FieldView withClassPropertyName(String classPropertyName) {
    return toBuilder().classPropertyName(classPropertyName).build();
  }

  public FieldView withDescription(String description) {
    return toBuilder().description(description).build();
  }

  public FieldView withFields(List<FieldView> fields) {
    return toBuilder().fields(fields).build();
  }

  public FieldView withIsArray(boolean isArray) {
    return toBuilder().isArray(isArray).build();
  }

  public FieldView withIsMap(boolean isMap) {
    return toBuilder().isMap(isMap).build();
  }

  public FieldView withGetterFuncName(String getterFuncName) {
    return toBuilder().getterFuncName(getterFuncName).build();
  }

  public FieldView withKeyValue(String keyValue) {
    return toBuilder().keyValue(keyValue).build();
  }

  public FieldView withTypeName(String typeName) {
    return toBuilder().typeName(typeName).build();
  }

  public FieldView withValue(String value) {
    return toBuilder().value(value).build();
  }

  public FieldView withVarName(String varName) {
    return toBuilder().varName(varName).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract FieldView build();

    public abstract Builder classPropertyName(String val);

    public abstract Builder description(String val);

    public abstract Builder discoveryFieldName(String val);

    public abstract Builder fieldName(String val);

    public abstract Builder fields(List<FieldView> val);

    public abstract Builder getterFuncName(String val);

    public abstract Builder setterFuncName(String val);

    public abstract Builder isArray(boolean val);

    public abstract Builder isMap(boolean val);

    public abstract Builder keyValue(String val);

    public abstract Builder required(boolean val);

    public abstract Builder typeName(String val);

    public abstract Builder value(String val);

    public abstract Builder varName(String val);
  }
}
