package com.enioka.jqm.webserver.serialization.jackson;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jakarta.rs.cfg.Annotations;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

/**
 * We need to configure the ObjectMapper before it is used by JAX-RS. Required because of a stupid map serialization format.
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonProvider extends JacksonJsonProvider
{
    protected static Logger jqmlogger = LoggerFactory.getLogger(JsonProvider.class);

    public JsonProvider()
    {
        super(getConfiguredMapper(), new Annotations[] { Annotations.JAKARTA_XML_BIND });
    }

    private static ObjectMapper getConfiguredMapper()
    {
        jqmlogger.debug("Registering custom Jackson JSON serializer/deserializer for maps");
        var builder = JsonMapper.builder();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Map.class, new MapDeSerializer());
        simpleModule.addSerializer(new MapSerializer());

        builder.addModule(simpleModule) // Add custom serializer/deserializer for maps
                .addModule(new JakartaXmlBindAnnotationModule()) // use XML annotations
                .enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME); // accept wrapper names

        return builder.build();
    }
}
