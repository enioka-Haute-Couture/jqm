/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

package com.enioka.jqm.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JobInstance;

class Mail
{
    private static Logger jqmlogger = Logger.getLogger(Mail.class);

    static void send(JobInstance ji, EntityManager em)
    {
        String to = ji.getEmail();
        String from = null;
        String host = null;
        int port = 25;
        // Final fields because they will be used in inner function
        final String pwd;
        final String userName;
        boolean useAuth = false;
        boolean useTLS = false;

        Map<String, String> params = new HashMap<String, String>();
        for (GlobalParameter gp : em.createQuery("SELECT gp FROM GlobalParameter gp", GlobalParameter.class).getResultList())
        {
            params.put(gp.getKey(), gp.getValue());
        }

        // Get basic parameters
        host = params.get("mailSmtpServer");
        from = params.get("mailFrom");
        try
        {
            port = Integer.parseInt(params.get("mailSmtpPort"));
        }
        catch (NumberFormatException e)
        {
            jqmlogger.warn("Parameter mailSmtpPort is not an integer. JQM can't send emails", e);
            return;
        }
        if (host == null || from == null)
        {
            jqmlogger.warn("Cannot send mails - incorrect configuration. Check parameters mailSmtpServer, mailFrom, mailSmtpPort.");
            return;
        }

        // Is there authentication information?
        pwd = params.get("mailSmtpPassword");
        if (pwd != null)
        {
            String un = params.get("mailSmtpUser");
            if (un != null)
            {
                userName = un;
            }
            else
            {
                userName = from;
            }
            useAuth = true;
            jqmlogger.debug("Mail will be sent using login " + userName + " and a password");
        }
        else
        {
            userName = null;
        }

        // SSL?
        useTLS = Boolean.parseBoolean(params.get("mailUseTls"));

        // Set properties
        jqmlogger.debug("An email will be sent to: " + to + " using server " + host + ":" + port + " (TLS: " + useTLS + " - auth: "
                + useAuth + ")");
        Properties props = new Properties();
        props.put("mail.smtp.auth", String.valueOf(useAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(useTLS));
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.connectiontimeout", 5000);
        props.put("mail.smtp.timeout", 5000);
        props.put("mail.smtp.writetimeout", 5000);

        // Create SMTP session
        Session session = null;
        if (useAuth)
        {
            session = Session.getInstance(props, new javax.mail.Authenticator()
            {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(userName, pwd);
                }
            });
        }
        else
        {
            session = Session.getInstance(props);
        }

        // Create message
        MimeMessage msg = new MimeMessage(session);
        try
        {
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject("[JQM] Job: " + ji.getId() + " ENDED");
            msg.setText("The Job number " + ji.getId() + " finished correctly\n\n" + "Job description:\n" + "- Job definition: "
                    + ji.getJd().getApplicationName() + "\n" + "- Parent: " + ji.getParentId() + "\n" + "- User name: " + ji.getUserName()
                    + "\n" + "- Session ID: " + ji.getSessionID() + "\n" + "- Queue: " + ji.getQueue().getName() + "\n" + "- Node: "
                    + ji.getNode().getName() + "\n" + "Best regards,\n");

            Transport.send(msg);
            jqmlogger.debug("Email sent successfully.");
        }
        catch (Exception e)
        {
            jqmlogger.warn("Could not send email. Job has nevertheless run correctly", e);
        }
    }
}
