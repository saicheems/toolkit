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

import com.google.api.codegen.SnippetSetRunner;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.auto.value.AutoValue;

// TODO: Notes for Sai:
// Naming semantics:
// - FieldName -> MyClass.MyVar
//                        ^^^^^
// - TypeName -> MyClass.MyVar
//               ^^^^^^^
// - VarName -> MyClass myClass = ...
//                      ^^^^^^^
// - PropertyName -> "myVar": { ... }
//                   ^^^^^^^
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

  public abstract ApiInfoView apiInfo();

  public abstract MethodInfoView methodInfo();

  public abstract String namespaceName(); // C#

  public abstract String className(); // C#

  // public abstract FieldView pageToken();
  // public abstract String pageTokenFieldName();

  // public abstract FieldView nextPageToken();
  // public abstract String nextPageTokenFieldName();

  // public abstract FieldView pageStreamingResource();
  // public abstract String pageStreamingResourceFieldName();
  // public abstract String pageStreamingResourceTypeName();
  // public abstract String pageStreamingResourceVarName();

  // public abstract List<FieldView> parameters();

  // public abstract FieldView requestBody();
  // public abstract String requestBodyTypeName();
  // public abstract String requestBodyVarName();

  // public abstract List<FieldView> requestBodyMembers();

  // public abstract FieldView response();
  // public abstract String responseTypeName();
  // public abstract String responseVarName();

  // public abstract FieldView service();
  // public abstract String serviceTypeName(); // C#
  // public abstract String serviceVarName(); // C#

  // public abstract List<String> usingAliasDirectives(); // C#

  // public abstract List<String> usingDirectives(); // C#

  @Override
  public abstract String templateFileName();

  @Override
  public abstract String outputPath();

  @Override
  public String resourceRoot() {
    return SnippetSetRunner.SNIPPET_RESOURCE_ROOT;
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder apiInfo(ApiInfoView val);

    public abstract Builder methodInfo(MethodInfoView val);

    public abstract Builder className(String val);

    public abstract Builder namespaceName(String val);

    public abstract Builder templateFileName(String val);

    public abstract Builder outputPath(String val);

    public abstract SampleView build();
  }
}
