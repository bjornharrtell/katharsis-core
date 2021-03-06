package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;

import java.io.IOException;
import java.util.*;

/**
 * Serializes top-level JSON object and provides ability to include compound documents
 */
public class BaseResponseSerializer extends JsonSerializer<BaseResponse> {

    private static final String INCLUDED_FIELD_NAME = "included";
    private static final String DATA_FIELD_NAME = "data";
    private static final String META_FIELD_NAME = "meta";

    private final ResourceRegistry resourceRegistry;
    private final IncludedRelationshipExtractor includedRelationshipExtractor;

    public BaseResponseSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;

        includedRelationshipExtractor = new IncludedRelationshipExtractor();
    }

    @Override
    public void serialize(BaseResponse value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Set<?> includedResources = new HashSet<>();

        gen.writeStartObject();
        if (value instanceof ResourceResponse) {
            Set included = serializeSingle((ResourceResponse) value, gen);
            //noinspection unchecked
            includedResources.addAll(included);
        } else if (value instanceof CollectionResponse) {
            Set included = serializeResourceCollection((CollectionResponse) value, gen);
            //noinspection unchecked
            includedResources.addAll(included);
        } else {
            throw new IllegalArgumentException(String.format("Response can be either %s or %s. Got %s",
                    ResourceResponse.class, CollectionResponse.class, value.getClass()));
        }

        gen.writeObjectField(INCLUDED_FIELD_NAME, includedResources);

        if (value.getMetaInformation() != null) {
            gen.writeObjectField(META_FIELD_NAME, value.getMetaInformation());
        }

        gen.writeEndObject();
    }

    private Set<?> serializeSingle(ResourceResponse resourceResponse, JsonGenerator gen) throws IOException {
        Object value = resourceResponse.getData();
        gen.writeObjectField(DATA_FIELD_NAME, new Container(value, resourceResponse.getRequestParams()));

        if (value != null) {
            Set<ResourceField> relationshipFields = getRelationshipFields(value);
            return includedRelationshipExtractor.extractIncludedResources(value, relationshipFields, resourceResponse);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    private Set<ResourceField> getRelationshipFields(Object resource) {
        Class<?> dataClass = resource.getClass();
        RegistryEntry entry = resourceRegistry.getEntry(dataClass);
        ResourceInformation resourceInformation = entry.getResourceInformation();
        return resourceInformation.getRelationshipFields();
    }

    private Set serializeResourceCollection(CollectionResponse collectionResponse, JsonGenerator gen) throws IOException {
        Iterable values = collectionResponse.getData();
        Set includedFields = new HashSet<>();
        if (values != null) {
            for (Object value : values) {
                Set<ResourceField> relationshipFields = getRelationshipFields(value);
                //noinspection unchecked
                includedFields.addAll(includedRelationshipExtractor.extractIncludedResources(value, relationshipFields, collectionResponse));
            }
        } else {
            values = Collections.emptyList();
        }

        List<Container> containers = new LinkedList<>();
        for (Object value : values) {
            containers.add(new Container(value, collectionResponse.getRequestParams()));
        }

        gen.writeObjectField(DATA_FIELD_NAME, containers);

        return includedFields;
    }

    public Class<BaseResponse> handledType() {
        return BaseResponse.class;
    }
}
