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

import com.google.api.codegen.SnippetSetRunner;
import com.google.api.codegen.viewmodel.ImportSectionView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class SampleView implements ViewModel {

  @Override
  public abstract String templateFileName();

  @Override
  public abstract String outputPath();

  @Override
  public String resourceRoot() {return SnippetSetRunner.SNIPPET_RESOURCE_ROOT;}

  public abstract ApiView api();

  public abstract String appName();

  public abstract List<String> imports();

  public abstract MethodView method();

  public abstract String className();

  public abstract String createServiceFunc();

  public static Builder newBuilder() {return new AutoValue_SampleView.Builder();}

  @AutoValue.Builder
  public class Builder {

    public abstract Builder api(ApiView val);

    public abstract Builder appName(String val);

    public abstract Builder imports(List<String> val);

    public abstract Builder method(MethodView val);

    public abstract Builder className(String val);

    public abstract Builder createServiceFunc(String val);
  }
}
