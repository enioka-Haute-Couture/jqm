package com.enioka.jqm.jndi;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.osgi.service.component.annotations.Component;

/**
 * This is a hack. By creating this stupid service, we allow other services to wait for the JNDI initialisation end (it is done in this
 * bundle activator). We could also use an event...
 */
@Component(service = InitialContext.class)
public class JndiWitnessService extends InitialContext
{
    public JndiWitnessService() throws NamingException
    {
        super();
    }
}
