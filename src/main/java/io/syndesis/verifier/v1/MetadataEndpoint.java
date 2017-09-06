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
package io.syndesis.verifier.v1;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import io.syndesis.verifier.v1.metadata.MetadataAdapter;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.impl.DefaultCamelContext;

abstract class MetadataEndpoint<T> {
    private final Map<String, MetadataAdapter> adapters;

    /* default */ MetadataEndpoint(final Map<String, MetadataAdapter> adapters) {
        this.adapters = adapters;
    }

    /* default */ final MetadataAdapter adapterFor(final String connectorId) {
        return Optional.ofNullable(adapters.get(connectorId + "-adapter"))
            .orElseThrow(() -> new IllegalStateException("Unable to fild adapter for:" + connectorId));
    }

    /* default */ DefaultCamelContext camelContext() {
        return new DefaultCamelContext();
    }

    /* default */ final T fetchMetadata(final String connectorId, final Map<String, Object> properties,
        final BiFunction<Map<String, Object>, MetaData, T> adapterMethod) {
        try {
            final CamelContext camel = camelContext();
            camel.start();

            try {
                final MetaDataExtension metadataExtension = camel.getComponent(connectorId, true, false)
                    .getExtension(MetaDataExtension.class).orElseThrow(() -> new IllegalArgumentException(
                        "No Metadata extension present for connector: " + connectorId));

                final MetaData metaData = metadataExtension.meta(properties)
                    .orElseThrow(() -> new IllegalArgumentException("No Metadata returned by the metadata extension"));

                return adapterMethod.apply(properties, metaData);
            } finally {
                camel.stop();
            }
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to fetch and process metadata", e);
        }
    }
}
