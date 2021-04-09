// Copyright 2005, 2006, 2007, 2009, 2012, 2016, 2018, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.mail;

import static javax.mail.Message.RecipientType.*;

import java.util.*;
import java.time.*;
import java.util.concurrent.*;
import java.security.GeneralSecurityException;
import java.security.interfaces.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataHandler;

import org.apache.log4j.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import org.deltava.beans.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.http.SendVAPIDPush;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A daemon thread to send e-mail and VAPID messages in the background. SMTP messages are not designed for critical information; they are
 * designed to fail silently on an error.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class MailerDaemon implements Runnable {

	private static final Logger log = Logger.getLogger(MailerDaemon.class);

	private static final int OK = 201;
	private static final Collection<Integer> INVALID_SUB_CODES = List.of(Integer.valueOf(404), Integer.valueOf(410));

	private Algorithm _jwtAlgo;
	private final String _code = SystemData.get("airline.code");
	private final String _vapidErrorsTo = String.format("mailto:%s", SystemData.get("airline.mail.webmaster"));

	private static final BlockingQueue<NotificationEnvelope<?>> _queue = new PriorityBlockingQueue<NotificationEnvelope<?>>();
	private final BlockingQueue<PushEndpoint> _invalidEndpoints = new LinkedBlockingQueue<PushEndpoint>();

	private static class SMTPAuth extends Authenticator {
		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(SystemData.get("smtp.user"), SystemData.get("smtp.pwd"));
		}
	}

	/**
	 * Queues an SMTP message for mailing by the daemon.
	 * @param env the SMTP envelope
	 */
	public static void push(NotificationEnvelope<?> env) {
		_queue.add(env);

		// Log receipients
		switch (env.getProtocol()) {
		case SMTP:
			SMTPEnvelope se = (SMTPEnvelope) env;
			Address[] addr = se.getRecipients();
			if (log.isDebugEnabled() && (addr != null) && (addr.length > 0))
				log.debug("Queued message for " + addr[0]);
			break;

		case VAPID:
			VAPIDEnvelope ve = (VAPIDEnvelope) env;
			log.debug("Queued message for " + ve.getEndpoint());
			break;

		default:
			log.warn("Unknown notification protocol - " + env.getProtocol());
		}
	}

	/**
	 * Returns any invalid push endpoints.
	 * @return a Collection of PushEndpoints
	 */
	public Collection<PushEndpoint> getInvalidEndpoints() {
		Collection<PushEndpoint> eps = new LinkedHashSet<PushEndpoint>();
		_invalidEndpoints.drainTo(eps);
		return eps;
	}

	private void send(VAPIDEnvelope env) throws DAOException, GeneralSecurityException {
		if (_jwtAlgo == null) return;
		if (_invalidEndpoints.contains(env.getEndpoint())) {
			log.warn("Skipping invalid endpoint " + env.getEndpoint());
			return;
		}

		// Build the JWT
		Instant expTime = Instant.now().plusSeconds(300);
		String jwt = JWT.create().withAudience(env.getAudience()).withSubject(_vapidErrorsTo).withExpiresAt(new Date(expTime.toEpochMilli())).sign(_jwtAlgo);
		env.setToken(jwt);

		// Encode the body if we need to
		byte[] data = VAPIDEncryptor.encrypt(env.getBody(), env.getEndpoint());
		Duration d = Duration.between(Instant.now(), env.getExpiryTime());
		if (d.isNegative()) {
			log.warn("Message already expired, skipping");
			return;
		}

		// Send the message
		SendVAPIDPush pwdao = new SendVAPIDPush(env.getEndpoint().getURL());
		int rspCode = pwdao.send(env.getToken(), data, d.toSeconds());
		if (INVALID_SUB_CODES.contains(Integer.valueOf(rspCode)))
			_invalidEndpoints.add(env.getEndpoint());
		else if (rspCode != OK)
			log.log((rspCode == OK) ? Level.INFO : Level.WARN, String.format("Returned %d sending to %s", Integer.valueOf(rspCode), env.getAudience()));
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

	@Override
	public void run() {
		log.info("Starting " + _code);
		boolean isAnon = StringUtils.isEmpty(SystemData.get("smtp.user"));

		// Set the SMTP server
		Properties props = new Properties(System.getProperties());
		props.put("mail.smtp.host", SystemData.get("smtp.server"));
		props.put("mail.smtp.auth", String.valueOf(!isAnon));
		props.put("mail.transport.protocol", "smtp");
		if (SystemData.getBoolean("smtp.tls")) {
			log.info("Enabling SMTP over TLS - " + (isAnon ? "anonymous" : "using credentials"));
			props.put("mail.smtp.port", String.valueOf(SystemData.getInt("smtp.port", 587)));
			props.put("mail.smtp.starttls.enable", "true");
		}

		// Init the session
		Session s = isAnon ? Session.getInstance(props) : Session.getInstance(props, new SMTPAuth());
		s.setDebug(SystemData.getBoolean("smtp.testMode"));

		// Load the VAPID keys and init the algorithm
		try {
			String rawPub = SystemData.get("security.key.push.pub");
			if (!StringUtils.isEmpty(rawPub)) {
				ECPublicKey ecPub = VAPIDEncryptor.fromX509Key(rawPub);
				ECPrivateKey ecPvt = VAPIDEncryptor.fromPCKS8Key(SystemData.get("security.key.push.pvt"));
				_jwtAlgo = Algorithm.ECDSA256(ecPub, ecPvt);
				log.info("Loaded VAPID encryption keys");
			} else
				log.warn("No encryption keys - VAPID disabled for " + _code);
		} catch (Exception e) {
			log.error("Error loading VAPID keys - " + e.getMessage(), e);
		}

		while (!Thread.currentThread().isInterrupted()) {
			try {
				NotificationEnvelope<?> env = _queue.take();
				try {
					switch (env.getProtocol()) {
					case SMTP:
						send(s, (SMTPEnvelope) env);
						break;

					case VAPID:
						send((VAPIDEnvelope) env);
						break;

					default:
						log.error("Unknown notification protocol - " + env.getProtocol());
						break;
					}
				} catch (Exception e) {
					log.error("Error sending " + env.getProtocol() + " message - " + e.getMessage(), e);
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}

		log.info("Stopping " + _code);
	}

	@Override
	public String toString() {
		return _code + " Mailer Daemon";
	}
}