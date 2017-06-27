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

import com.google.api.codegen.SnippetSetRunner;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;

// TODO: Notes for Sai:
// Naming semantics:
// - getClassPropertyName -> MyClass.MyVar <- C# only right now
//                                ^^^^^
// - TypeName -> MyClass.MyVar
//               ^^^^^^^
// - VarName -> MyClass myClass = ...
//                      ^^^^^^^
// - FieldName -> "myVar": { ... }
//                ^^^^^^^
// - DiscoveryFieldName -> "foo": { ... } <- use if it's the actual field name in discovery
//                         ^^^^^
// - FuncName -> function();       <- use if method is not on an object
//               ^^^^^^^^
// - MethodName -> myObj.method(); <- use if method is on an object
//                       ^^^^^^
// Append with name if the return type is a String.

@AutoValue
public abstract class SampleView implements ViewModel {

  public static Builder newBuilder() {
    return new AutoValue_SampleView.Builder();
  }

  @Override
  public abstract String outputPath();

  @Override
  public String resourceRoot() {
    return SnippetSetRunner.SNIPPET_RESOURCE_ROOT;
  }

  @Override
  public abstract String templateFileName();

  public abstract ApiInfoView apiInfo();

  public abstract MethodInfoView methodInfo();

  public abstract String appName();

  public abstract List<UsingDirectiveView> usingDirectives(); // C#

  public abstract List<UsingDirectiveView> usingAliasDirectives(); // C#

  public abstract String sampleClassName(); // C#

  public abstract String sampleNamespaceName(); // C#

  public abstract FieldView service();

  public abstract List<FieldView> parameters();

  @Nullable
  public abstract FieldView requestBody();

  public abstract FieldView request();

  @Nullable
  public abstract FieldView response();

  @Nullable
  public abstract FieldView pageStreamingResource();

  @Nullable
  public abstract FieldView pageToken();

  @Nullable
  public abstract FieldView nextPageToken();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder templateFileName(String val);

    public abstract Builder outputPath(String val);

    public abstract Builder apiInfo(ApiInfoView val);

    public abstract Builder methodInfo(MethodInfoView val);

    public abstract Builder appName(String val);

    public abstract Builder usingDirectives(List<UsingDirectiveView> val);

    public abstract Builder usingAliasDirectives(List<UsingDirectiveView> val);

    public abstract Builder sampleClassName(String val);

    public abstract Builder sampleNamespaceName(String val);

    public abstract Builder service(FieldView val);

    public abstract Builder parameters(List<FieldView> val);

    public abstract Builder requestBody(FieldView val);

    public abstract Builder request(FieldView val);

    public abstract Builder response(FieldView val);

    public abstract Builder pageStreamingResource(FieldView val);

    public abstract Builder pageToken(FieldView val);

    public abstract Builder nextPageToken(FieldView val);

    public abstract SampleView build();
  }
}
