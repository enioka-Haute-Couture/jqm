package com.enioka.jqm.handler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

import com.enioka.jqm.api.JobManager;

public class JobManagerProvider implements ObjectFactory<JobManager>
{
    @Override
    public JobManager getObject() throws BeansException
    {
        return AnnotationSpringContextBootstrapHandler.localJm.get();
    }
}
