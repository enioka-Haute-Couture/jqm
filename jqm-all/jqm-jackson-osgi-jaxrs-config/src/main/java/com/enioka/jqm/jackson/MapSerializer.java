package com.enioka.jqm.jackson;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * A custom dedeserializer to allow to keep our stupid legacy [ {key: 'key', value: 'value'}, ...] format for maps.
 */
public class MapSerializer extends StdSerializer<Map<?, ?>>
{
    protected MapSerializer()
    {
        super(Map.class, false);
    }

    @Override
    public void serialize(Map<?, ?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException
    {
        gen.writeStartArray();
        for (Map.Entry<?, ?> pair : value.entrySet())
        {
            gen.writeStartObject();
            gen.writeStringField("key", pair.getKey().toString());
            gen.writeStringField("value", pair.getValue().toString());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }
}
