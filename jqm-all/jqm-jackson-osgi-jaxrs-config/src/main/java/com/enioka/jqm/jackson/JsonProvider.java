package com.enioka.jqm.jackson;

import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jakarta.rs.cfg.Annotations;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;

/**
 * We need to configure the ObjectMapper before it is used by JAX-RS. As Jackson's provider auto-registration is ignored inside an OSGi
 * context, this class allows us to directly register writer & reader as OSGi services. (we could also register the feature after
 * configuration instead)
 */
@Provider
@JakartarsExtension
@Component(service = { MessageBodyReader.class, MessageBodyWriter.class })
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonProvider extends JacksonJsonProvider
{
    public JsonProvider()
    {
        super(getConfiguredMapper(), new Annotations[] { Annotations.JAKARTA_XML_BIND });
    }

    private static ObjectMapper getConfiguredMapper()
    {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Map.class, new MapDeSerializer());
        simpleModule.addSerializer(new MapSerializer());
        mapper.registerModule(simpleModule);
        mapper.registerModule(new JakartaXmlBindAnnotationModule());
        mapper.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
        return mapper;
    }
}
