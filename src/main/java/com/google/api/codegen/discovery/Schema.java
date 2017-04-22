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
package com.google.api.codegen.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import javax.annotation.Nullable;

/**
 * A representation of a Discovery Document schema.
 *
 * <p>Note that this class is not necessarily a 1-1 mapping of the official specification.
 */
@AutoValue
public abstract class Schema {

  /**
   * Returns true if this schema contains a property with the given name.
   *
   * @param name the name of the property.
   * @return whether or not this schema has a property with the given name.
   */
  public boolean hasProperty(String name) {
    return properties().keySet().contains(name);
  }

  /**
   * Returns a schema constructed from root, or an empty schema if root has no children.
   *
   * @param root the root node to parse.
   * @return a schema.
   */
  public static Schema from(DiscoveryNode root) {
    if (root.isEmpty()) {
      return empty();
    }
    Schema additionalProperties = Schema.from(root.getObject("additionalProperties"));
    String defaultValue = root.getString("default");
    String description = root.getString("description");
    Format format = Format.getEnum(root.getString("format"));
    String id = root.getString("id");
    String location = root.getString("location");
    String pattern = root.getString("pattern");

    ImmutableMap.Builder<String, Schema> propertiesBuilder = ImmutableMap.builder();
    DiscoveryNode propertiesNode = root.getObject("properties");
    for (String name : propertiesNode.fieldNames()) {
      propertiesBuilder.put(name, Schema.from(propertiesNode.getObject(name)));
    }

    String reference = root.getString("$ref");
    boolean repeated = root.getBoolean("repeated");
    boolean required = root.getBoolean("required");
    Type type = Type.getEnum(root.getString("type"));

    return new AutoValue_Schema(
        additionalProperties,
        defaultValue,
        description,
        format,
        id,
        location,
        pattern,
        propertiesBuilder.build(),
        reference,
        repeated,
        required,
        type);
  }

  public static Schema empty() {
    ImmutableMap<String, Schema> properties = ImmutableMap.of();
    return new AutoValue_Schema(
        null, "", "", Format.EMPTY, "", "", "", properties, "", false, false, Type.EMPTY);
  }

  /** @return the schema of the additionalProperties, or null if none. */
  @JsonProperty("additionalProperties")
  @Nullable
  public abstract Schema additionalProperties();

  /** @return the default value. */
  @JsonProperty("defaultValue")
  public abstract String defaultValue();

  /** @return the description. */
  @JsonProperty("description")
  public abstract String description();

  /** @return the format. */
  @JsonProperty("format")
  public abstract Format format();

  /** @return the ID. */
  @JsonProperty("id")
  public abstract String id();

  /** @return the location. */
  @JsonProperty("location")
  public abstract String location();

  /** @return the pattern. */
  @JsonProperty("pattern")
  public abstract String pattern();

  /** @return the map of property names to schemas. */
  @JsonProperty("properties")
  public abstract ImmutableMap<String, Schema> properties();

  /** @return the reference. */
  @JsonProperty("reference")
  public abstract String reference();

  /** @return whether or not the schema is repeated. */
  @JsonProperty("repeated")
  public abstract boolean repeated();

  /** @return whether or not the schema is required. */
  @JsonProperty("required")
  public abstract boolean required();

  /** @return the type. */
  @JsonProperty("type")
  public abstract Type type();

  /** The set of types a schema can represent. */
  public enum Type {
    ANY("any"),
    ARRAY("array"),
    BOOLEAN("boolean"),
    EMPTY(""),
    INTEGER("integer"),
    NUMBER("number"),
    OBJECT("object"),
    STRING("string");

    private String text;

    Type(String text) {
      this.text = text;
    }

    /**
     * @param text the JSON text of the type.
     * @return the enum representing the raw JSON type.
     */
    public static Type getEnum(String text) {
      for (Type t : values()) {
        if (t.text.equals(text)) {
          return t;
        }
      }
      throw new IllegalArgumentException("unknown type: " + text);
    }
  }

  /** The set of formats a schema can represent. */
  public enum Format {
    BYTE("byte"),
    DATE("date"),
    DATETIME("date-time"),
    DOUBLE("double"),
    EMPTY(""),
    FLOAT("float"),
    INT32("int32"),
    INT64("int64"),
    UINT32("uint32"),
    UINT64("uint64"),
    UNKNOWN("");

    private String text;

    Format(String text) {
      this.text = text;
    }

    /**
     * @param text the JSON text of the format.
     * @return the enum representing the raw JSON format.
     */
    public static Format getEnum(String text) {
      if (text.isEmpty()) {
        return EMPTY;
      }
      for (Format f : values()) {
        if (f.text.equals(text)) {
          return f;
        }
      }
      // TODO(saicheems): Warn about unknown formats? I saw "google-datetime" in pubsub for example.
      return UNKNOWN;
    }
  }
}
