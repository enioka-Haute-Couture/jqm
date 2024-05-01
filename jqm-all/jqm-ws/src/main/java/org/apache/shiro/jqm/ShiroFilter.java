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
package org.apache.shiro.jqm;

import jakarta.servlet.Filter;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.ws.api.Helpers;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterAsyncSupported;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter extends the usual ShiroFilter by checking in the database if security should be enabled or not. When enabled by OSGi, it has
 * the highest ranking possible in order to be called before the web services.
 *
 */
@Component(service = Filter.class, property = { org.osgi.framework.Constants.SERVICE_RANKING + ":Integer=" + 5,
        "osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=MAIN_HTTP_CTX)" })
@HttpWhiteboardFilterPattern("/*")
@HttpWhiteboardFilterAsyncSupported
public class ShiroFilter extends org.apache.shiro.web.servlet.ShiroFilter
{
    private static Logger jqmlogger = LoggerFactory.getLogger(ShiroFilter.class);

    @Override
    public void init() throws Exception
    {
        // Load the ini file from current CL to avoid CL-hell in OSGi.
        String enableWsApiAuth = this.getContextInitParam("enableWsApiAuth");
        boolean load = true;

        if (enableWsApiAuth != null)
        {
            // Self-hosted mode: parameter is given by the server
            load = Boolean.parseBoolean(enableWsApiAuth);
        }
        else
        {
            // Hosted in a standard servlet container - fetch from db.
            try (DbConn cnx = Helpers.getDbSession())
            {
                load = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableWsApiAuth", "true"));
            }
        }

        jqmlogger.debug("Shiro filter enabled: " + load + " - " + enableWsApiAuth);
        setEnabled(load);
        super.init();
    }
}
