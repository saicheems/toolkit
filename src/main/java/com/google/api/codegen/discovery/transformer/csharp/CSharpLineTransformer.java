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
package com.google.api.codegen.discovery.transformer.csharp;

import com.google.api.codegen.discovery.Method;
import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.discovery.viewmodel.LineView;
import java.util.ArrayList;

public class CSharpLineTransformer {

  public static LineView generateFieldLine(Method method, Schema parameter) {
    return LineView.newBuilder().lines(new ArrayList<LineView>()).text("").build(); // TODO
  }

  public static LineView generateRequestLine(Method method) {
    return LineView.newBuilder().lines(new ArrayList<LineView>()).text("").build(); // TODO
  }

  public static LineView generateRequestBodyLine(Method method) {
    return LineView.newBuilder().lines(new ArrayList<LineView>()).text("").build(); // TODO
  }

  public static LineView generateResponseLine(Method method) {
    return LineView.newBuilder().lines(new ArrayList<LineView>()).text("").build(); // TODO
  }
}
