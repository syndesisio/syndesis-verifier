/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.verifier.v1.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;

import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.springframework.stereotype.Component;

@Component("salesforce-adapter")
public final class SalesforceMetadataAdapter implements MetadataAdapter {

    private static final String UNIQUE_PROPERTY = "sObjectIdName";

    @Override
    public Map<String, List<PropertyPair>> adaptForProperties(final Map<String, Object> properties,
        final MetaData metadata) {
        final String scope = (String) metadata.getAttribute("scope");

        if ("object".equals(scope)) {
            return adaptForObjectRequest(properties, metadata);
        } else if ("object_types".equals(scope)) {
            return adaptForObjectTypeRequest(properties, metadata);
        }

        throw new IllegalStateException(
            "Unknown or missing `scope` attribute in Salesforce MetaData, scope is: " + scope);
    }

    @Override
    public Map<SchemaUse, ObjectSchema> adaptForSchema(final Map<String, Object> properties, final MetaData metadata) {
        final ObjectSchema schema = objectSchemaFrom(metadata.getPayload(ObjectSchema.class));

        final HashMap<SchemaUse, ObjectSchema> ret = new HashMap<>(2);
        ret.put(SchemaUse.INPUT, schema);
        ret.put(SchemaUse.OUTPUT, schema);

        return ret;
    }

    static Map<String, List<PropertyPair>> adaptForObjectRequest(final Map<String, Object> properties,
        final MetaData metadata) {
        final Map<String, List<PropertyPair>> ret = new HashMap<>();

        final ObjectSchema schema = objectSchemaFrom(metadata.getPayload(ObjectSchema.class));
        if (properties.containsKey(UNIQUE_PROPERTY)) {
            final List<PropertyPair> uniquePropertyPairs = schema.getProperties().entrySet().stream()
                .filter(e -> isIdLookup(e.getValue()))
                .map(SalesforceMetadataAdapter::createFieldPairPropertyFromSchemaEntry).collect(Collectors.toList());

            ret.put(UNIQUE_PROPERTY, uniquePropertyPairs);
        }

        return ret;
    }

    static Map<String, List<PropertyPair>> adaptForObjectTypeRequest(final Map<String, Object> properties,
        final MetaData metadata) {
        final JsonNode payload = metadata.getPayload(JsonNode.class);

        final List<PropertyPair> objects = StreamSupport.stream(payload.spliterator(), false)
            .map(SalesforceMetadataAdapter::createObjectPairPropertyFromNode).collect(Collectors.toList());

        return Collections.singletonMap("sObjectName", objects);
    }

    static PropertyPair createFieldPairPropertyFromSchemaEntry(final Entry<String, JsonSchema> e) {
        return new PropertyPair(e.getKey(), ((SimpleTypeSchema) e.getValue()).getTitle());
    }

    static PropertyPair createObjectPairPropertyFromNode(final JsonNode node) {
        final String value = node.get("name").asText();
        final String displayValue = node.get("label").asText();

        return new PropertyPair(value, displayValue);
    }

    static boolean isIdLookup(final JsonSchema property) {
        final String description = property.getDescription();

        if (description == null) {
            return false;
        }

        return description.contains("idLookup");
    }

    static ObjectSchema objectSchemaFrom(final ObjectSchema schema) {
        if (schema.getOneOf().isEmpty()) {
            return schema;
        }

        return (ObjectSchema) schema.getOneOf().stream().filter(ObjectSchema.class::isInstance)
            .filter(s -> !((ObjectSchema) s).getId().contains(":QueryRecords")).findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "The resulting schema does not contain an non query records object schema in `oneOf`"));
    }

}
