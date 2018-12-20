// Copyright 2005, 2006, 2007, 2009, 2012, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.mail;

import static javax.mail.Message.RecipientType.*;

import java.util.*;
import java.util.concurrent.*;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.log4j.Logger;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A daemon thread to send e-mail messages in the background. SMTP messages are not designed for critical information;
 * they are designed to fail silently on an error.
 * @author Luke
 * @version 8.5
 * @since 1.0
 */

public class MailerDaemon implements Runnable {

	private static final Logger log = Logger.getLogger(MailerDaemon.class);

	private static final BlockingQueue<SMTPEnvelope> _queue = new PriorityBlockingQueue<SMTPEnvelope>();
	
	private static class SMTPAuth extends Authenticator {
		
		SMTPAuth() {
			super();
		}
		
		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(SystemData.get("mail.smtp.user"), SystemData.get("mail.smtp.pwd"));
		}
	}
	
	/**
	 * Queues an SMTP message for mailing by the daemon.
	 * @param env the SMTP envelope
	 */
	public static void push(SMTPEnvelope env) {
		_queue.add(env);

		// Log receipients
		Address[] addr = env.getRecipients();
		if (log.isDebugEnabled() && (addr != null) && (addr.length > 0))
			log.debug("Queued message for " + addr[0]);
	}

	private static void send(Session s, SMTPEnvelope env) {
		if ((env == null) || (!env.hasRecipients()))
			return;

		// Create the message
		MimeMessage imsg = new MimeMessage(s);
		try {
			imsg.setFrom(new InternetAddress(env.getFrom().getEmail(), env.getFrom().getName()));
			imsg.addHeader("Errors-to", SystemData.get("mail.smtp.errors-to"));
			imsg.setSubject(env.getSubject(), "UTF-8");
			imsg.setRecipients(TO, env.getRecipients());
			for (Map.Entry<String, String> he : env.getHeaders().entrySet())
				imsg.addHeader(he.getKey(), he.getValue());
			if (env.getCopyTo() != null)
				imsg.addRecipients(CC, env.getCopyTo());
		} catch (Exception e) {
			log.error("Error setting message headers - " + e.getMessage(), e);
			return;
		}

		// Set the message content
		try {
			Multipart mp = new MimeMultipart();

			// Get the encoding type
			String enc = env.getContentType().substring(env.getContentType().lastIndexOf('/') + 1);
			
			// Add message body
			MimeBodyPart body = new MimeBodyPart();
			body.setText(env.getBody(), "UTF-8", enc);
			mp.addBodyPart(body);

			// If we have an attachment, add it
			if (env.getAttachment() != null) {
				MimeBodyPart fa = new MimeBodyPart();
				fa.setDataHandler(new DataHandler(env.getAttachment()));
				fa.setFileName(env.getAttachment().getName());
				mp.addBodyPart(fa);
			}

			imsg.setContent(mp);
			imsg.setSentDate(new Date(env.getCreatedOn().toEpochMilli()));
		} catch (MessagingException me) {
			log.error("Error setting message content - " + me.getMessage(), me);
		}

		// Send the message
		try {
			Transport.send(imsg);
			log.info("Sent message to " + env);
		} catch (Exception e) {
			log.error("Error sending email to " + env, e);
		}
	}

	/**
	 * Executes the Thread.
	 */
	@Override
	public void run() {
		log.info("Starting");
		boolean isAnon = StringUtils.isEmpty(SystemData.get("smtp.user"));

		// Set the SMTP server
		Properties props = new Properties(System.getProperties());
		props.put("mail.smtp.host", SystemData.get("smtp.server"));
		if (SystemData.getBoolean("smtp.tls")) {
			log.info("Enabling SMTP over TLS - " + (isAnon ? "anonymous" : "using credentials"));
			props.put("mail.smtp.port", "465");
			props.put("mail.smtp.starttls.enable", "true");
		}
		
		while (!Thread.currentThread().isInterrupted()) {
			try {
				SMTPEnvelope env = _queue.take();

				// Generate a session to the STMP server
				try {
					Session s = isAnon ? Session.getInstance(props) : Session.getInstance(props, new SMTPAuth());
					s.setDebug(SystemData.getBoolean("smtp.testMode"));

					// Loop through the messages if we have them
					while (env != null) {
						send(s, env);
						env = _queue.poll();
					}
				} catch (Exception e) {
					log.error("Error connecting to STMP server " + e.getMessage());
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}

		log.info("Stopping");
	}
	
	/**
	 * Returns the thread name.
	 * @return the tread name
	 */
	@Override
	public String toString() {
		return SystemData.get("airline.code") + " Mailer Daemon";
	}
}