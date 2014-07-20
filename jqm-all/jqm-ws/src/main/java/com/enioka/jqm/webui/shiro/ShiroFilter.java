/**
 * Copyright © 2013 enioka. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            load = Boolean.parseBoolean(Helpers.getParameter("enableWsApiAuth", "true", em));
        }
        finally
        {
            Helpers.closeQuietly(em);
        }

        setEnabled(load);
        super.init();
    }
}
