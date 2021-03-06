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
package com.google.api.codegen.viewmodel;

/** The concrete callable class type */
public enum ApiCallableImplType {
  SimpleApiCallable(ServiceMethodType.UnaryMethod),
  PagedApiCallable(ServiceMethodType.UnaryMethod),
  BundlingApiCallable(ServiceMethodType.UnaryMethod),
  StreamingApiCallable(ServiceMethodType.GrpcStreamingMethod),
  InitialOperationApiCallable(ServiceMethodType.UnaryMethod),
  OperationApiCallable(ServiceMethodType.LongRunningMethod);

  private ServiceMethodType serviceMethodType;

  ApiCallableImplType(ServiceMethodType serviceMethodType) {
    this.serviceMethodType = serviceMethodType;
  }

  public ServiceMethodType serviceMethodType() {
    return serviceMethodType;
  }
}
