package com.enioka.jqm.ws.jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

/**
 * A custom deserializer to allow to keep our stupid legacy [ {key: 'key', value: 'value'}, ...] format for maps.
 */
public class MapDeSerializer extends JsonDeserializer<Map<?, ?>> implements ContextualDeserializer
{
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException
    {
        JavaType wrapperType = property.getType();
        JavaType valueType1 = wrapperType.containedType(0);
        JavaType valueType2 = wrapperType.containedType(1);

        if (valueType1.isTypeOrSubTypeOf(String.class) && valueType2.isTypeOrSubTypeOf(String.class))
        {
            return new MapDeSerializer();
        }
        else
        {
            return null; // TODO: may return MapDeserializer from Jackson instead.
        }
    }

    @Override
    public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        Map<String, String> res = new HashMap<>();

        if (!p.getCurrentToken().equals(JsonToken.START_ARRAY))
        {
            throw new JsonParseException(p, "Map must beging with array delimiter");
        }

        JsonToken curToken = p.nextToken();
        while (!curToken.equals(JsonToken.END_ARRAY))
        {
            String key = null, value = null;
            boolean keyField = false;

            if (!curToken.equals(JsonToken.START_OBJECT))
            {
                throw new JsonParseException(p, "Map must be a list of objects - missing start");
            }
            curToken = p.nextToken();
            if (!curToken.equals(JsonToken.FIELD_NAME))
            {
                throw new JsonParseException(p, "Map must be a list of objects - missing field name 1");
            }
            keyField = p.getValueAsString().equals("key");

            curToken = p.nextToken();
            if (!curToken.equals(JsonToken.VALUE_STRING))
            {
                throw new JsonParseException(p, "Map must be a list of objects - missing field string value 1");
            }
            if (keyField)
            {
                key = p.getValueAsString();
            }
            else
            {
                value = p.getValueAsString();
            }

            curToken = p.nextToken();
            if (!curToken.equals(JsonToken.FIELD_NAME))
            {
                throw new JsonParseException(p, "Map must be a list of objects - missing field name 2");
            }
            keyField = p.getValueAsString().equals("key");
            curToken = p.nextToken();
            if (!curToken.equals(JsonToken.VALUE_STRING))
            {
                throw new JsonParseException(p, "Map must be a list of objects - missing field string value 2");
            }
            if (keyField)
            {
                key = p.getValueAsString();
            }
            else
            {
                value = p.getValueAsString();
            }

            res.put(key, value);

            curToken = p.nextToken();
            if (!curToken.equals(JsonToken.END_OBJECT))
            {
                throw new JsonParseException(p, "Map must be a list of objects - missing end got " + p.getText());
            }
            curToken = p.nextToken();
        }

        return res;
    }
}
