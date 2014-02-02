package com.enioka.jqm.api;

import java.util.Properties;

final class PropertiesHandler
{
    private PropertiesHandler()
    {

    }

    static void addDefaults(Properties p)
    {
        // If no connection data, use a fixed JNDI alias.
        if (!p.containsKey("hibernate.connection.datasource") && !p.containsKey("javax.persistence.jdbc.url"))
        {
            p.put("hibernate.connection.datasource", p.getProperty("hibernate.connection.datasource", "jdbc/jqm"));
        }
    }

}
