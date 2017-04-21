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
package com.google.api.codegen.discovery.viewmodel;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PageStreamingView {

  public abstract boolean isResourceMap();

  public abstract boolean isResourceRepeated();

  public abstract boolean isResourceSetterInRequestBody();

  public abstract String nextPageTokenGetterFunc();

  public abstract String pageTokenGetterFunc();

  public abstract String resourceGetterFunc();

  public abstract String resourceVar();

  public abstract String resourceType();

  public static Builder newBuilder() {return new AutoValue_PageStreamingView.Builder();}

  @AutoValue.Builder
  public abstract class Builder {

    public abstract Builder isResourceMap(boolean val);

    public abstract Builder isResourceRepeated(boolean val);

    public abstract Builder isResourceSetterInRequestBody(boolean val);

    public abstract Builder nextPageTokenGetterFunc(String val);

    public abstract Builder pageTokenGetterFunc(String val);

    public abstract Builder resourceGetterFunc(String val);

    public abstract Builder resourceVar(String val);

    public abstract Builder resourceType(String val);
  }
}
