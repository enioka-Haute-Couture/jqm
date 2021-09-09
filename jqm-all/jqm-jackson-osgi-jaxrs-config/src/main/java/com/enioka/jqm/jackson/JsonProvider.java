package com.enioka.jqm.jackson;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsExtension;

/**
 * We need to configure the ObjectMapper before it is used by JAX-RS. As Jackson's provider auto-registration is ignored inside an OSGi
 * context, this class allows us to directly register writer & reader as OSGi services. (we could also register the feature after
 * configuration instead)
 */
@Provider
@JaxrsExtension
@Component(service = { MessageBodyReader.class, MessageBodyWriter.class })
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonProvider extends JacksonJsonProvider
{
    public JsonProvider()
    {
        super(getConfiguredMapper(), new Annotations[] { Annotations.JAXB });
    }

    private static ObjectMapper getConfiguredMapper()
    {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Map.class, new MapDeSerializer());
        simpleModule.addSerializer(new MapSerializer());
        mapper.registerModule(simpleModule);
        mapper.registerModule(new JaxbAnnotationModule());
        mapper.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
        return mapper;
    }
}
