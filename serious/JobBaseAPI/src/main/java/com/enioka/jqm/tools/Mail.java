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

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Node;

public class Mail
{
	private Logger jqmlogger = Logger.getLogger(Mail.class);
	private String to = null;
	private String from = null;
	private String host = null;
	private Node node = null;
	private JobInstance ji = null;
	private String username = "pico.2607@gmail.com";
	private String pwd = "toto";

	public Mail(Node node, JobInstance ji)
	{
		this.to = "pico.2607@gmail.com";
		this.from = "pico.2607@gmail.com";
		this.host = node.getListeningInterface();
		this.node = node;
		this.ji = ji;
	}

	public void send()
	{
		jqmlogger.debug("Preparation of the email");
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator()
		{
			@Override
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(username, pwd);
			}
		});

		MimeMessage msg = new MimeMessage(session);
		try
		{
			msg.setFrom(new InternetAddress(username));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));
			msg.setSubject("[JQM] Job: " + ji.getId() + " ENDED");
			msg.setText("The Job number: " + ji.getId() + " finished correctly\n" + "Description of the job:\n" + "- Job definition: "
					+ ji.getJd().getApplicationName() + "\n" + "- Parent: " + ji.getParent() + "\n" + "- User name: " + ji.getUserName()
					+ "\n" + "- Session ID: " + ji.getSessionID() + "\n" + "- Queue: " + ji.getQueue() + "\n" + "- Node: " + ji.getNode()
					+ "\n" + "Best regards,\n");

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
