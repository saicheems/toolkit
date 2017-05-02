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

public class CSharpNamerTest {

  /*@Test
  public void testGetServiceTypeFullNameWithoutNamespace() {
    CSharpNamer namer = new CSharpNamer("class");
    Truth.assertThat(namer.getServiceTypeFullNameWithoutNamespace()).isEqualTo("ClassClassService");
    namer = new CSharpNamer("Class");
    Truth.assertThat(namer.getServiceTypeFullNameWithoutNamespace()).isEqualTo("ClassService");
  }

  @Test
  public void testGetObjectTypeFullNameWithoutNamespace() {
    CSharpNamer namer = new CSharpNamer("class");

    String className = namer.getObjectTypeFullNameWithoutNamespace("MySchema", "schemas.mySchema");
    Truth.assertThat(className).isEqualTo("MySchema");

    ImmutableMap<String, String> noIdTestCases =
        new ImmutableMap.Builder<String, String>()
            .put("schemas.class", "Class")
            .put("schemas.a.items", "AItems")
            .put("schemas.a.items.a.items", "AItems.AData")
            .put("schemas.a.properties.b", "A.BData")
            .put("schemas.b.properties.b.properties.b.properties.b", "B.BData.BDataSchema.BData")
            .put("schemas.a.properties.b.properties.c.additionalProperties", "A.BData.CDataElement")
            .build();

    for (String key : noIdTestCases.keySet()) {
      String actual = namer.getObjectTypeFullNameWithoutNamespace("", key);
      String expected = noIdTestCases.get(key);
      Truth.assertThat(actual).isEqualTo(expected);
    }
  }

  @Test
  public void testGetRequestTypeFullNameWithoutNamespace() {
    CSharpNamer namer = new CSharpNamer("class");

    ImmutableMap<String, String> testCases =
        new ImmutableMap.Builder<String, String>()
            .put("resources.a.resources.b.methods.c", "AResource.BResource.CRequest")
            .put("resources.int.methods.using", "ClassIntResource.ClassUsingRequest")
            .put("methods.a", "ARequest")
            .build();

    for (String key : testCases.keySet()) {
      String actual = namer.getRequestTypeFullNameWithoutNamespace(key);
      String expected = testCases.get(key);
      Truth.assertThat(actual).isEqualTo(expected);
    }
  }

  @Test
  public void testGetTopLevelEnumParameterTypeFullNameWithoutNamespace() {
    CSharpNamer namer = new CSharpNamer("class");

    ImmutableMap<String, String> testCases =
        new ImmutableMap.Builder<String, String>()
            .put(
                "resources.int.methods.using.parameters.enum",
                "ClassIntResource.ClassUsingRequest.ClassEnumEnum")
            .put("methods.a.parameters.b", "ARequest.BEnum")
            .build();

    for (String key : testCases.keySet()) {
      String actual = namer.getTopLevelEnumParameterTypeFullNameWithoutNamespace(key);
      String expected = testCases.get(key);
      Truth.assertThat(actual).isEqualTo(expected);
    }
  }

  @Test
  public void testGetServiceRequestFuncFullName() {
    CSharpNamer namer = new CSharpNamer("goog");

    ImmutableMap<String, String> testCases =
        new ImmutableMap.Builder<String, String>()
            .put("resources.int.resources.using.methods.c", "GoogInt.GoogUsing.C")
            .put("methods.a", "A")
            .build();

    for (String key : testCases.keySet()) {
      String actual = namer.getServiceRequestFuncFullName(key);
      String expected = testCases.get(key);
      Truth.assertThat(actual).isEqualTo(expected);
    }
  }*/
}
