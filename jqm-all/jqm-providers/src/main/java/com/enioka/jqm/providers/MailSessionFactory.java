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
package com.enioka.jqm.providers;

import java.util.Hashtable;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * JavaMail Session factory for SMTP (or SMTPS) mail servers.<br>
 * Resource parameters are:
 * <ul>
 * <li>smtpServerHost</li>
 * <li>smtpServerPort - default is 25</li>
 * <li>fromAddress - default is noreply@jobs.org</li>
 * <li>smtpUser - default is null, which means no authentication</li>
 * <li>smtpPassword - default is null, which means no authentication</li>
 * <li>useTls - default is false</li>
 * <li>timeout - default is 5000ms. It is used for connection timeout, session timeout and write timeout</li>
 * </ul>
 */
public class MailSessionFactory implements ObjectFactory
{
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
    {
        Reference resource = (Reference) obj;

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", god(resource, "smtpServerHost", "google.smtp.com"));
        props.put("mail.smtp.port", god(resource, "smtpServerPort", "25"));
        props.put("mail.smtp.starttls.enable", god(resource, "useTls", "false"));
        props.put("mail.smtp.from", god(resource, "fromAddress", "noreply@jobs.org"));

        String timeout = god(resource, "timeout", "5000");
        props.put("mail.smtp.connectiontimeout", timeout);
        props.put("mail.smtp.timeout", timeout);
        props.put("mail.smtp.writetimeout", timeout);

        // Authentication?
        String user = god(resource, "smtpUser", null);
        String passwd = god(resource, "smtpPassword", null);

        Authenticator auth = null;
        if (user != null && passwd != null)
        {
            final PasswordAuthentication pa = new PasswordAuthentication(user, passwd);
            props.put("mail.smtp.auth", "true");
            auth = new Authenticator()
            {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return pa;
                }
            };
        }

        // Done
        Session session = Session.getInstance(props, auth);
        return session;
    }

    private String god(Reference resource, String key, String defaultValue)
    {
        RefAddr val = resource.get(key);
        String res = (String) (val == null ? null : val.getContent());
        if (res == null)
        {
            return defaultValue;
        }
        else
        {
            return res;
        }
    }
}
