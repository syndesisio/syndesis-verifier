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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import io.syndesis.verifier.v1.metadata.MetadataAdapter;
import io.syndesis.verifier.v1.metadata.MetadataAdapter.SchemaUse;

import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ActionShapesEndpointTest {
    private static final Map<String, String> PAYLOAD = Collections.singletonMap("this", "is playload");

    private static final Map<MetadataAdapter.SchemaUse, ObjectSchema> SCHEMAS = new HashMap<>();

    private final ActionShapesEndpoint endpoint = new ActionShapesEndpoint(
        Collections.singletonMap("petstore-adapter", new PetstoreAdapter(PAYLOAD, null, SCHEMAS))) {
        @Override
        protected DefaultCamelContext camelContext() {
            final DefaultCamelContext camelContext = new DefaultCamelContext();
            camelContext.addComponent("petstore", new PetstoreComponent(PAYLOAD));

            return camelContext;
        }
    };

    @Test
    public void shouldProvideActionPropertiesBasedOnMetadata() throws Exception {
        final Map<SchemaUse, ObjectSchema> shapes = endpoint.shape("petstore", Collections.emptyMap());

        assertThat(shapes).isSameAs(SCHEMAS);
    }
}
