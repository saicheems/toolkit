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

import com.google.api.codegen.discovery.viewmodel.LineView;
import com.google.api.codegen.discovery.viewmodel.MediaView;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class MethodView {

  public abstract List<LineView> fields();

  public abstract boolean hasMediaDownload();

  public abstract boolean hasMediaUpload();

  public abstract boolean hasRequestBody();

  public abstract boolean hasResponse();

  public abstract boolean isPageStreaming();

  public abstract boolean isScopesSingular();

  public abstract LineView request();

  public abstract LineView requestBody();

  public abstract String resourceGetterFunc();

  public abstract LineView response();

  public abstract String responseVar();

  public abstract List<String> scopes();

  public static Builder newBuilder() {return new AutoValue_MethodView.Builder()}

  @AutoValue.Builder
  public abstract class Builder {

    public abstract Builder fields(List<LineView> val);

    public abstract Builder hasMediaDownload(boolean val);

    public abstract Builder hasRequestBody(boolean val);

    public abstract Builder hasResponse(boolean val);

    public abstract Builder isPageStreaming(boolean val);

    public abstract Builder isScopesSingular(boolean val);

    public abstract Builder request(LineView val);

    public abstract Builder resourceGetterFunc(String val);

    public abstract Builder response(LineView val);

    public abstract Builder responseVar(String val);

    public abstract Builder scopes(List<String> val);
  }
}
