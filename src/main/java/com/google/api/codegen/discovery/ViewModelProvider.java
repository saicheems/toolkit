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
package com.google.api.codegen.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.api.codegen.discovery.transformer.SampleTransformer;
import com.google.api.codegen.rendering.CommonSnippetSetRunner;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.api.tools.framework.snippet.Doc;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * Calls the MethodToViewTransformer on a method with the provided ApiaryConfig.
 *
 * Responsible for generating the Document, merging with optional overrides,
 * and calling the MethodToViewTransformer.
 */
public class ViewModelProvider implements DiscoveryProvider {

  private final Document document;
  private final CommonSnippetSetRunner snippetSetRunner;
  private final SampleTransformer sampleTransformer;
  private final List<JsonNode> overrides;
  private final String outputRoot;

  private ViewModelProvider(
      Document document,
      CommonSnippetSetRunner snippetSetRunner,
      SampleTransformer methodToViewTransformer,
      List<JsonNode> overrides,
      String outputRoot) {
    this.document = document;
    this.snippetSetRunner = snippetSetRunner;
    this.sampleTransformer = methodToViewTransformer;
    this.overrides = overrides;
    this.outputRoot = outputRoot;
  }

  @Override
  public Map<String, Doc> generate(Method method) {
    Document overriddenDocument = override(document, overrides);
    ViewModel surfaceDoc = sampleTransformer.transform(overriddenDocument, method);
    Doc doc = snippetSetRunner.generate(surfaceDoc);
    Map<String, Doc> docs = new TreeMap<>();
    if (doc == null) {
      return docs;
    }
    docs.put(outputRoot + "/" + surfaceDoc.outputPath(), doc);
    return docs;
  }

  /**
   * Applies overrides to document.
   *
   * <p>If overrides is null, document is returned as is.
   */
  private static Document override(Document document, List<JsonNode> overrides) {
    if (overrides.isEmpty()) {
      return document;
    }
    // We use JSON merging to facilitate this override mechanism:
    // 1. Convert the Document into a JSON tree.
    // 2. Convert the overrides into JSON trees with arbitrary schema.
    // 3. Overwrite object fields of the Document tree where field names match.
    // 4. Convert the modified Document tree back into a Document.
    ObjectMapper mapper = new ObjectMapper().registerModule(new GuavaModule());
    JsonNode tree = mapper.valueToTree(document);
    for (JsonNode override : overrides) {
      merge((ObjectNode) tree, (ObjectNode) override);
    }
    try {
      return mapper.treeToValue(tree, Document.class);
    } catch (Exception e) {
      throw new RuntimeException("failed to parse config to node: " + e.getMessage());
    }
  }

  /**
   * Overwrites the fields of tree that intersect with those of overrideTree.
   *
   * <p>Values of tree are modified, and values of overrideTree are not modified. The merge process
   * loops through the fields of tree and replaces non-object values with the corresponding value
   * from overrideTree if present. Object values are traversed recursively to replace
   * sub-properties. Null fields in overrideTree are removed from tree.
   *
   * @param tree the original JsonNode to modify.
   * @param overrideTree the JsonNode with values to overwrite tree with.
   */
  private static void merge(ObjectNode tree, ObjectNode overrideTree) {
    Iterator<String> fieldNames = overrideTree.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonNode defaultValue = tree.get(fieldName);
      JsonNode overrideValue = overrideTree.get(fieldName);
      if (defaultValue == null) {
        // If backupValue isn't null, then we add it to tree. This can happen if
        // extra fields/properties are specified.
        if (overrideValue != null) {
          tree.set(fieldName, overrideValue);
        }
        // Otherwise, skip null nodes.
      } else if (overrideValue.isNull()) {
        // If a node is overridden as null, we pretend it was never specified
        // altogether. We provide this functionality so nodes from an object can
        // be deleted from both trees.
        // TODO(saicheems): Verify that this is the best approach for this issue.
        tree.remove(fieldName);
      } else if (defaultValue.isObject() && overrideValue.isObject()) {
        merge((ObjectNode) defaultValue, (ObjectNode) overrideValue.deepCopy());
      } else {
        tree.set(fieldName, overrideTree.get(fieldName));
      }
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Document document;
    private CommonSnippetSetRunner snippetSetRunner;
    private SampleTransformer sampleTransformer;
    private List<JsonNode> overrides;
    private String outputRoot;

    private Builder() {}

    public Builder setDocument(Document document) {
      this.document = document;
      return this;
    }

    public Builder setSnippetSetRunner(CommonSnippetSetRunner snippetSetRunner) {
      this.snippetSetRunner = snippetSetRunner;
      return this;
    }

    public Builder setMethodToViewTransformer(SampleTransformer sampleTransformer) {
      this.sampleTransformer = sampleTransformer;
      return this;
    }

    public Builder setOverrides(List<JsonNode> overrides) {
      this.overrides = overrides;
      return this;
    }

    public Builder setOutputRoot(String outputRoot) {
      this.outputRoot = outputRoot;
      return this;
    }

    public ViewModelProvider build() {
      return new ViewModelProvider(
          document, snippetSetRunner, sampleTransformer, overrides, outputRoot);
    }
  }
}
