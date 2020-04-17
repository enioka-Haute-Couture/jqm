package com.enioka.jqm.handler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

import java.util.Map;

public class ParametersProvider implements ObjectFactory<Map<String, String>>
{
    @Override
    public Map<String, String> getObject() throws BeansException
    {
        return AnnotationSpringContextBootstrapHandler.localJm.get().parameters();
    }
}
