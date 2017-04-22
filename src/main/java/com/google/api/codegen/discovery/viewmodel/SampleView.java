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
import java.util.List;

// TODO: Notes for Sai:
// Naming semantics:
// - FieldName -> MyClass.MyVar
//                        ^^^^^
// - TypeName -> MyClass.MyVar
//               ^^^^^^^
// - VarName -> MyClass myClass = ...
//              ^^^^^^^
// - PropertyName -> "myVar": { ... }
//                   ^^^^^^^
// Append with name if the return type is a String.

@AutoValue
public abstract class SampleView implements ViewModel {

  public static Builder newBuilder() {
    return new AutoValue_SampleView.Builder();
  }

  public abstract ApiInfoView apiInfo();

  public abstract String appName();

  public abstract String className(); // C#

  public abstract List<LineView> fieldLines();

  public abstract List<String> imports();

  public abstract MethodInfoView methodInfo();

  public abstract String namespaceName(); // C#

  public abstract String nextPageTokenFieldName();

  @Override
  public abstract String outputPath();

  public abstract String pageStreamingResourceFieldName();

  public abstract String pageStreamingResourceTypeName();

  public abstract String pageStreamingResourceVarName();

  public abstract String pageTokenFieldName();

  public abstract LineView requestLine();

  public abstract LineView requestBodyLine();

  @Override
  public String resourceRoot() {
    return SnippetSetRunner.SNIPPET_RESOURCE_ROOT;
  }

  public abstract LineView responseLine();

  public abstract String responseVarName();

  public abstract String serviceTypeName(); // C#

  public abstract String serviceVarName(); // C#

  @Override
  public abstract String templateFileName();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder apiInfo(ApiInfoView val);

    public abstract Builder appName(String val);

    public abstract SampleView build();

    public abstract Builder className(String val);

    public abstract Builder fieldLines(List<LineView> val);

    public abstract Builder imports(List<String> val);

    public abstract Builder methodInfo(MethodInfoView val);

    public abstract Builder namespaceName(String val);

    public abstract Builder nextPageTokenFieldName(String val);

    public abstract Builder outputPath(String val);

    public abstract Builder pageStreamingResourceFieldName(String val);

    public abstract Builder pageStreamingResourceTypeName(String val);

    public abstract Builder pageStreamingResourceVarName(String val);

    public abstract Builder pageTokenFieldName(String val);

    public abstract Builder requestLine(LineView val);

    public abstract Builder requestBodyLine(LineView val);

    public abstract Builder responseLine(LineView val);

    public abstract Builder responseVarName(String val);

    public abstract Builder serviceTypeName(String val);

    public abstract Builder serviceVarName(String val);

    public abstract Builder templateFileName(String val);
  }
}
