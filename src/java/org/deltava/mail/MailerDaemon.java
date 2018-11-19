// Copyright 2005, 2006, 2007, 2009, 2012, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.mail;

import java.util.*;
import java.util.concurrent.*;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.log4j.Logger;

import org.deltava.util.system.SystemData;

/**
 * A daemon thread to send e-mail messages in the background. SMTP messages are not designed for critical information;
 * they are designed to fail silently on an error.
 * @author Luke
 * @version 8.4
 * @since 1.0
 */

public class MailerDaemon implements Runnable {

	private static final Logger log = Logger.getLogger(MailerDaemon.class);

	private static final BlockingQueue<SMTPEnvelope> _queue = new PriorityBlockingQueue<SMTPEnvelope>();

	/**
	 * Returns the thread name.
	 * @return the tread name
	 */
	@Override
	public String toString() {
		return SystemData.get("airline.code") + " Mailer Daemon";
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
			imsg.addHeader("Errors-to", SystemData.get("smtp.errors-to"));
			imsg.setSubject(env.getSubject(), "UTF-8");
			imsg.setRecipients(javax.mail.Message.RecipientType.TO, env.getRecipients());
			if (env.getCopyTo() != null)
				imsg.addRecipients(javax.mail.Message.RecipientType.CC, env.getCopyTo());
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

		// Set the SMTP server
		Properties props = new Properties(System.getProperties());
		props.put("mail.smtp.host", SystemData.get("smtp.server"));
		if (SystemData.getBoolean("smtp.tls")) {
			log.info("Enabling SMTP over TLS");
			props.put("mail.smtp.port", "465");
			props.put("mail.smtp.ssl.enable", "true");
			props.put("mail.smtp.starttls.enable", "true");
		}
		
		while (!Thread.currentThread().isInterrupted()) {
			try {
				SMTPEnvelope env = _queue.take();

				// Generate a session to the STMP server
				try {
					Session s = Session.getInstance(props);
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
}