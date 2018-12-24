// Copyright 2005, 2006, 2009, 2010, 2014, 2015, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.mail;

import java.util.*;

import javax.activation.DataSource;

import org.apache.log4j.Logger;

import org.deltava.beans.EMailAddress;
import org.deltava.util.MailUtils;
import org.deltava.util.system.SystemData;

/**
 * A utility class to send e-mail messages.
 * @author Luke
 * @version 8.5
 * @since 1.0
 */

public class Mailer {

	private static final Logger log = Logger.getLogger(Mailer.class);

	private final SMTPEnvelope _env;
	private final Collection<EMailAddress> _msgTo = new LinkedHashSet<EMailAddress>();
	private MessageContext _ctx;

	/**
	 * Initializes the mailer with a source address.
	 * @param from the source address
	 */
	public Mailer(EMailAddress from) {
		super();
		boolean isOurs = (from != null) && MailUtils.getDomain(from.getEmail()).equalsIgnoreCase(SystemData.get("airline.domain"));
		_env = new SMTPEnvelope(!isOurs ? MailUtils.makeAddress(SystemData.get("airline.mail.webmaster"), SystemData.get("airline.name")) : from);
	}

	/**
	 * Attaches a file to the message.
	 * @param ds a DataSource pointing to the file attachment data.
	 */
	public void setAttachment(DataSource ds) {
		_env.setAttachment(ds);
	}

	/**
	 * Sets the messaging context used to generate e-mail messages.
	 * @param ctx the messaging context
	 */
	public void setContext(MessageContext ctx) {
		_ctx = ctx;
	}

	/**
	 * Adds an individual address to the recipient list, and sends the message <i>in a new thread</i>.
	 * @param addr the recipient name/address
	 */
	public void send(EMailAddress addr) {
		if ((addr != null) && !addr.isInvalid()) {
			_msgTo.add(addr);
			send();
		}
	}

	/**
	 * Adds an individual to the CC list of this message.
	 * @param addr the recipient name/address
	 */
	public void setCC(EMailAddress addr) {
		_env.addCopyTo(addr);
	}

	/**
	 * Adds a group of addresses to the recipient list, and sends the message <i>in a new thread</i>.
	 * @param addrs a Collection of recipient names/addresses
	 */
	public void send(Collection<? extends EMailAddress> addrs) {
		for (EMailAddress addr : addrs) {
			if (!addr.isInvalid())
				_msgTo.add(addr);
		}
		
		send();
	}

	/*
	 * Generates and sends the e-mail message. The recipient object is added to the messaging context under the name
	 * &quot;recipient&quot;.
	 */
	private void send() {
		// If we're in test mode, send back to the sender only
		if (SystemData.getBoolean("smtp.testMode")) {
			log.warn("STMP Test Mode enabled - sending to " + _env.getFrom().getEmail());
			_msgTo.clear();
			_msgTo.add(_env.getFrom());
			_env.clearRecipients();
		}

		// Warn if we have no template
		try {
			_ctx.getBody();
		} catch (IllegalStateException ise) {
			log.error("Message Template not loaded");
			return;
		}

		// Loop through the recipients
		_env.addHeader("X-Golgotha-template", _ctx.getTemplate().getName());
		_env.addHeader("X-Golgotha-mass", String.valueOf(_msgTo.size() > 1));
		for (EMailAddress addr : _msgTo) {
			_env.setRecipient(addr);
			_ctx.setRecipient(addr);
			_env.setSubject(_ctx.getSubject());
			_env.setBody(_ctx.getBody()); // calculate body and subject

			// Determine the content type
			_env.setContentType((_ctx.getTemplate() != null) && _ctx.getTemplate().getIsHTML() ? "text/html" : "text/plain");

			// Queue the message up
			MailerDaemon.push((SMTPEnvelope) _env.clone());
		}

		// Clear out addresses
		_msgTo.clear();
	}
}