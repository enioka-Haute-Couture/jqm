package com.enioka.jqm.tools;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Node;

public class Mail
{
	private Logger jqmlogger = Logger.getLogger(Mail.class);
	private String to = null;
	private String from = null;
	private String host = null;
	private JobInstance ji = null;
	private String port = null;
	private String pwd = "marsu1952";

	public Mail(Node node, JobInstance ji, EntityManager em)
	{
		try
		{
			this.host = em.createQuery("SELECT gp.value FROM GlobalParameter gp WHERE gp.key = :k", String.class)
					.setParameter("k", "mailSmtp").getSingleResult();
			this.to = "jqm.noreply@gmail.com";
			this.from = em.createQuery("SELECT gp.value FROM GlobalParameter gp WHERE gp.key = :k", String.class)
					.setParameter("k", "mailFrom").getSingleResult();
			this.ji = ji;
			this.port = em.createQuery("SELECT gp.value FROM GlobalParameter gp WHERE gp.key = :k", String.class)
					.setParameter("k", "mailPort").getSingleResult();
		} catch (NoResultException e)
		{
			jqmlogger.debug("Some information have been forgotten. JQM can't send emails", e);
		}
	}

	public void send()
	{
		jqmlogger.debug("Preparation of the email");
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);

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
			msg.setText("The Job number: " + ji.getId() + " finished correctly\n" + "Description of the job:\n" + "- Job definition: "
					+ ji.getJd().getApplicationName() + "\n" + "- Parent: " + ji.getParent() + "\n" + "- User name: " + ji.getUserName()
					+ "\n" + "- Session ID: " + ji.getSessionID() + "\n" + "- Queue: " + ji.getQueue().getName() + "\n" + "- Node: "
					+ ji.getNode().getListeningInterface() + "\n" + "Best regards,\n");

			Transport.send(msg);
			jqmlogger.debug("Email sent successfully...");
		} catch (AddressException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
