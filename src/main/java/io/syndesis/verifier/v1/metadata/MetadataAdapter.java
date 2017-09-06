package io.syndesis.verifier.v1.metadata;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;

/**
 * Converting metadata from Camel components to applicable properties or
 * generating ObjectSchema from Metadata is specific to each connector. This
 * adapter bridges Camel {@link MetaDataExtension} Component specific
 * implementations to common Syndesis data model.
 */
public interface MetadataAdapter {

    enum SchemaUse {
        INPUT, OUTPUT
    }

    /**
     * Converts Camel {@link MetaDataExtension.MetaData} to a Map keyed by
     * action property name with a list of {@link PropertyPair} values that are
     * applicable to for that property. Method will receive all properties that
     * client specified and the retrieved {@link MetaDataExtension.MetaData}
     * from the appropriate Camel {@link MetaDataExtension}.
     *
     * @param properties properties specified on the endpoint
     * @param metadata the retrieved metadata
     * @return map keyed by action property of all pairs that can be used as
     *         values of that property
     */
    Map<String, List<PropertyPair>> adaptForProperties(Map<String, Object> properties, MetaData metadata);

    /**
     * Converts Camel {@link MetaDataExtension.MetaData} to a map of
     * {@link ObjectSchema}'s keyed by the use of the schema. Method will
     * receive all properties that client specified and the retrieved
     * {@link MetaDataExtension.MetaData} from the appropriate Camel
     * {@link MetaDataExtension}.
     *
     * @param properties properties specified on the endpoint
     * @param metadata the retrieved metadata
     * @return map keyed by the use of the schema
     */
    Map<SchemaUse, ObjectSchema> adaptForSchema(Map<String, Object> properties, MetaData metadata);
}