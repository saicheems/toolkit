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

import com.google.api.codegen.discovery.Document;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ApiInfoView {

  public static Builder newBuilder() {
    return new AutoValue_ApiInfoView.Builder();
  }

  public abstract String authInstructionsUrl();

  public abstract Document.AuthType authType();

  public abstract String discoveryDocUrl();

  public abstract String name();

  public abstract String title();

  public abstract String version();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder authInstructionsUrl(String val);

    public abstract Builder authType(Document.AuthType val);

    public abstract ApiInfoView build();

    public abstract Builder discoveryDocUrl(String val);

    public abstract Builder name(String val);

    public abstract Builder title(String val);

    public abstract Builder version(String val);
  }
}
