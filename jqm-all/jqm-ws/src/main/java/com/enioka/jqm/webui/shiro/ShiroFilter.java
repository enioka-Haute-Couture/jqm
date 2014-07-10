package com.enioka.jqm.webui.shiro;

import java.util.Properties;

import javax.persistence.EntityManager;

import com.enioka.jqm.api.Helpers;
import com.enioka.jqm.api.JqmClientFactory;

/**
 * This filter extends the usual ShiroFilter by checking in the database if security should be enabled or not.
 * 
 */
public class ShiroFilter extends org.apache.shiro.web.servlet.ShiroFilter
{
    static
    {
        Properties p = new Properties();
        p.put("javax.persistence.nonJtaDataSource", "jdbc/jqm");
        JqmClientFactory.setProperties(p);
    }

    @Override
    public void init() throws Exception
    {
        EntityManager em = Helpers.getEm();
        boolean load = true;
        try
        {
            load = Boolean.parseBoolean(Helpers.getParameter("useAuth", "true", em));
        }
        finally
        {
            Helpers.closeQuietly(em);
        }

        setEnabled(load);
        super.init();
    }
}
