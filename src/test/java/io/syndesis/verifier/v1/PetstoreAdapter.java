package io.syndesis.verifier.v1;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import io.syndesis.verifier.v1.metadata.MetadataAdapter;
import io.syndesis.verifier.v1.metadata.PropertyPair;

import org.apache.camel.component.extension.MetaDataExtension.MetaData;

import static org.assertj.core.api.Assertions.assertThat;

public class PetstoreAdapter implements MetadataAdapter {

    Map<SchemaUse, ObjectSchema> adaptedSchema;

    private final Map<String, List<PropertyPair>> adaptedProperties;

    private final Map<String, String> expectedPayload;

    public PetstoreAdapter(final Map<String, String> expectedPayload,
        final Map<String, List<PropertyPair>> adaptedProperties, final Map<SchemaUse, ObjectSchema> adaptedSchema) {
        this.adaptedProperties = adaptedProperties;
        this.expectedPayload = expectedPayload;
        this.adaptedSchema = adaptedSchema;
    }

    @Override
    public Map<String, List<PropertyPair>> adaptForProperties(final Map<String, Object> properties,
        final MetaData metadata) {
        @SuppressWarnings("unchecked")
        final Map<String, String> payload = metadata.getPayload(Map.class);

        assertThat(payload).isSameAs(expectedPayload);

        return adaptedProperties;
    }

    @Override
    public Map<SchemaUse, ObjectSchema> adaptForSchema(final Map<String, Object> properties, final MetaData metadata) {
        @SuppressWarnings("unchecked")
        final Map<String, String> payload = metadata.getPayload(Map.class);

        assertThat(payload).isSameAs(expectedPayload);

        return adaptedSchema;
    }

}