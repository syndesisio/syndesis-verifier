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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import io.syndesis.verifier.v1.metadata.MetadataAdapter;
import io.syndesis.verifier.v1.metadata.MetadataAdapter.SchemaUse;

import org.springframework.stereotype.Component;

@Component
@Path("/action/shapes")
public class ActionShapesEndpoint extends MetadataEndpoint<Map<MetadataAdapter.SchemaUse, ObjectSchema>> {

    public ActionShapesEndpoint(final Map<String, MetadataAdapter> adapters) {
        super(adapters);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{connectorId}")
    public Map<SchemaUse, ObjectSchema> shape(@PathParam("connectorId") final String connectorId,
        final Map<String, Object> properties) throws Exception {
        final MetadataAdapter adapter = adapterFor(connectorId);

        return fetchMetadata(connectorId, properties, adapter::adaptForSchema);
    }

}
