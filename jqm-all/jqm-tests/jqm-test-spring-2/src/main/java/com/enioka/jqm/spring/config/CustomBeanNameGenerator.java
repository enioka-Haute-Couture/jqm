package com.enioka.jqm.spring.config;

import java.beans.Introspector;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

public class CustomBeanNameGenerator extends AnnotationBeanNameGenerator
{
    @Override
    protected String buildDefaultBeanName(BeanDefinition definition)
    {
        String fqClassName = definition.getBeanClassName();
        return Introspector.decapitalize(fqClassName);
    }
}
