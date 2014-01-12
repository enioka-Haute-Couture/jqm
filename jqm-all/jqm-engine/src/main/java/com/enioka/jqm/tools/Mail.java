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

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.JobInstance;

class Mail
{
    private Logger jqmlogger = Logger.getLogger(Mail.class);
    private String to = null;
    private String from = null;
    private String host = null;
    private JobInstance ji = null;
    private String port = null;
    private String pwd = "marsu1952";

    Mail(JobInstance ji, EntityManager em)
    {
        try
        {
            this.ji = ji;
            this.to = ji.getEmail();

            String query = "SELECT gp.value FROM GlobalParameter gp WHERE gp.key = :k";
            this.host = em.createQuery(query, String.class).setParameter("k", "mailSmtp").getSingleResult();
            this.from = em.createQuery(query, String.class).setParameter("k", "mailFrom").getSingleResult();
            this.port = em.createQuery(query, String.class).setParameter("k", "mailPort").getSingleResult();
        }
        catch (NoResultException e)
        {
            jqmlogger.warn("Some JQM configuration data is missing. JQM can't send emails", e);
        }
    }

    void send()
    {
        if (this.to == null || this.host == null || this.from == null || this.port == null)
        {
            jqmlogger.warn("cannot send mails - incorrect configuration");
            return;
        }
        jqmlogger.debug("Preparation of the email");
        jqmlogger.debug("The email will be sent to: " + to);
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.connectiontimeout", 5000);
        props.put("mail.smtp.timeout", 5000);
        props.put("mail.smtp.writetimeout", 5000);

        Session session = Session.getInstance(props, new javax.mail.Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(to, pwd);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        try
        {
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject("[JQM] Job: " + ji.getId() + " ENDED");
            msg.setText("The Job number " + ji.getId() + " finished correctly\n\n" + "Job description:\n" + "- Job definition: "
                    + ji.getJd().getApplicationName() + "\n" + "- Parent: " + ji.getParentId() + "\n" + "- User name: " + ji.getUserName()
                    + "\n" + "- Session ID: " + ji.getSessionID() + "\n" + "- Queue: " + ji.getQueue().getName() + "\n" + "- Node: "
                    + ji.getNode().getListeningInterface() + "\n" + "Best regards,\n");

            Transport.send(msg);
            jqmlogger.debug("Email sent successfully...");
        }
        catch (Exception e)
        {
            jqmlogger.warn("Could not send email. Job has nevertheless run correctly", e);
        }
    }
}
