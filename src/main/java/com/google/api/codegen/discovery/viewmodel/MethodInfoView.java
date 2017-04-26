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

import com.google.api.codegen.discovery.Schema;
import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class MethodInfoView {

  public static Builder newBuilder() {
    return new AutoValue_MethodInfoView.Builder();
  }

  public abstract boolean hasRequestBody();

  public abstract boolean hasResponse();

  public abstract boolean isPageStreaming();

  public abstract boolean isScopesSingular();

  public abstract String pageStreamingResourceFieldName();

  public abstract Schema.Type pageStreamingResourceType();

  public abstract String parametersPageTokenFieldName();

  public abstract String requestBodyPageTokenFieldName();

  public abstract String responsePageTokenFieldName();

  public abstract List<String> scopes();

  public abstract boolean supportsMediaDownload();

  public abstract boolean supportsMediaUpload();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract MethodInfoView build();

    public abstract Builder hasRequestBody(boolean val);

    public abstract Builder hasResponse(boolean val);

    public abstract Builder isPageStreaming(boolean val);

    public abstract Builder isScopesSingular(boolean val);

    public abstract Builder pageStreamingResourceFieldName(String val);

    public abstract Builder pageStreamingResourceType(Schema.Type val);

    public abstract Builder parametersPageTokenFieldName(String val);

    public abstract Builder requestBodyPageTokenFieldName(String val);

    public abstract Builder responsePageTokenFieldName(String val);

    public abstract Builder scopes(List<String> val);

    public abstract Builder supportsMediaDownload(boolean val);

    public abstract Builder supportsMediaUpload(boolean val);
  }
}
