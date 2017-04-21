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

import java.util.List;

// Intended to support blocks like:
// String x = "hello";
//
// List<String> y = new ArrayList<>();
// y.add("world");  // Sub-statement.

// A convoluted example:
//
// A list of Foos for...
// List<Foo> foos = new ArrayList<>();  // root
//
// // A foo is a ...
// Foo foo = new Foo();                 // root -> sub[0]
//
// // A bar is a ...
// Bar bar = new Bar();                 // root -> sub[0] -> sub[0]
// bar.setBaz(0);                       // root -> sub[0] -> sub[0] -> sub[0]
// bar.setJaz("yo");                    // root -> sub[0] -> sub[0] -> sub[1]
//
// foo.setBar(bar);                     // root -> sub[0] -> sub[1]
//
// foos.add(foo);                       // root -> sub[1]

// Using a different tree walk algorithm:
//
// Bar bar = new Bar();
// bar.setBaz(0);
// bar.setJaz("yo");
//
// Foo foo = new Foo();
// foo.setBar(bar);

// List<Foo> foos = new ArrayList<>();
// foos.add(foo);

// Also can support dicts:
// {
//   'hello': {     // root
//     'world': [   // root -> sub[0]
//       'woohoo!'  // root -> sub[0] -> sub[0]
//     ],           // root -> sub[1]
//   }              // root -> sub[2]
// }

// TODO: Only print the "TODO" on leaves!!! ^^^

@AutoValue
public abstract class LineView {

  public abstract List<LineView> lines();

  public abstract String text();

  public static Builder newBuilder() {return new AutoValue_LineView.Builder();}

  @AutoValue.Builder
  public abstract class Builder {

    public abstract Builder lines(List<LineView> val);

    public abstract Builder text(String val);
  }
}
