// Copyright 2005, 2006, 2009, 2010, 2014, 2015, 2016, 2018, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.mail;

import java.util.*;
import java.time.Instant;

import javax.activation.DataSource;

import org.apache.log4j.Logger;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A utility class to send e-mail messages.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class Mailer {

	private static final Logger log = Logger.getLogger(Mailer.class);

	private final SMTPEnvelope _env;
	private final boolean _multiChannel;
	private final Collection<EMailAddress> _msgTo = new LinkedHashSet<EMailAddress>();
	private final Collection<PushEndpoint> _pushTo = new LinkedHashSet<PushEndpoint>();
	private MessageContext _ctx;

	/**
	 * Initializes the mailer with a source address.
	 * @param from the source address
	 */
	public Mailer(EMailAddress from) {
		this(from, SystemData.getBoolean("smtp.multiChannel") || !SystemData.has("smtp.multiChannel")); // default to true, not false
	}
	
	/**
	 * Initializes the mailer with a source address.
	 * @param from the source address
	 * @param multiChannel TRUE to send both push and email to same recipient, otherwise FALSE
	 */
	public Mailer(EMailAddress from, boolean multiChannel) {
		super();
		_multiChannel = multiChannel || !SystemData.has("security.key.push.pub");
		String domain = SystemData.get("airline.domain");
		boolean isOurs = (from != null) && MailUtils.getDomain(from.getEmail()).equalsIgnoreCase(domain);
		_env = new SMTPEnvelope(isOurs, !isOurs ? MailUtils.makeAddress(SystemData.get("airline.mail.webmaster"), SystemData.get("airline.name")) : from);
		if (!isOurs)
			_env.addHeader("Reply-To", MailUtils.makeAddress("no-reply", domain, "DO NOT REPLY").getEmail());
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
	 * Adds an individual address to the recipient list, and sends the message in a new thread.
	 * @param addr the recipient name/address
	 */
	public void send(EMailAddress addr) {
		if (addRecipient(addr)) send();
	}
	
	/**
	 * Adds an individual address to the recipient list.
	 * @param addr the recipient name/address
	 * @return TRUE if there is a valid address to send to
	 */
	public boolean addRecipient(EMailAddress addr) {
		boolean doSend = false;
		if (addr instanceof PushAddress)
			doSend |= _pushTo.addAll(((PushAddress)addr).getPushEndpoints());
		if (EMailAddress.isValid(addr) && (_multiChannel || !doSend))
			doSend |= _msgTo.add(addr);
		
		return doSend;
	}

	/**
	 * Adds an individual to the CC list of this message.
	 * @param addr the recipient name/address
	 */
	public void setCC(EMailAddress addr) {
		if (EMailAddress.isValid(addr))
			_env.addCopyTo(addr);
	}

	/**
	 * Adds a group of addresses to the recipient list, and sends the message <i>in a new thread</i>.
	 * @param addrs a Collection of recipient names/addresses
	 */
	public void send(Collection<? extends EMailAddress> addrs) {
		boolean anySent = addrs.stream().filter(this::addRecipient).findAny().isPresent();
		if (anySent) send();
	}
	
	private void sendSMTP(EMailAddress addr) {
		_env.setRecipient(addr);
		_ctx.setRecipient(addr);
		_env.setSubject(_ctx.getSubject());
		_env.setBody(_ctx.getBody()); // calculate body and subject
		
		// Determine the content type and queue it up
		_env.setContentType((_ctx.getTemplate() != null) && _ctx.getTemplate().getIsHTML() ? "text/html" : "text/plain");
		MailerDaemon.push((SMTPEnvelope) _env.clone());
	}
	
	private void sendPush(PushEndpoint ep) {
		
		JSONObject mo = new JSONObject();
		mo.put("lang", "en");
		mo.put("requireInteraction", true);
		mo.put("title", SystemData.get("airline.name"));
		mo.put("body", _ctx.getSubject());
		mo.put("icon", String.format("/%s/favicon/favicon-32x32.png", SystemData.get("path.img")));
		
		// Get context object
		Object ID = Integer.valueOf(ep.getID());
		MessageTemplate mt = _ctx.getTemplate();
		if (!StringUtils.isEmpty(mt.getNotifyContext())) {
			Object ctx = _ctx.evaluate(mt.getNotifyContext()); 
			if (ctx instanceof DatabaseBean)
				ID = Integer.valueOf(((DatabaseBean) ctx).getID());
			else
				log.warn(String.format("Context object %s not a DatabaseBean - %s", mt.getNotifyContext(), ctx.getClass().getName()));
		}
		
		// Add the actions
		for (NotifyActionType at : mt.getActionTypes()) {
			NotifyAction act = NotifyAction.create(at, ID);
			JSONObject ao = new JSONObject();
			ao.put("title", act.getDescription());
			ao.put("action", act.getURL());
			ao.put("id", ID);
			mo.accumulate("actions", ao);
		}
		
		// Add a URL if no other actions
		if (mt.getActionTypes().isEmpty())
			mo.put("url", String.format("https://%s", SystemData.get("airline.url")));

		// Create the envelope
		JSONUtils.ensureArrayPresent(mo, "actions");
		VAPIDEnvelope env = new VAPIDEnvelope(ep);
		env.setExpirationTime(Instant.now().plusSeconds(mt.getNotificationTTL()));
		env.setBody(mo.toString());
		MailerDaemon.push(env);
	}

	/*
	 * Generates and sends the e-mail message. The recipient object is added to the messaging context under the name &quot;recipient&quot;.
	 */
	private void send() {
		if (_msgTo.isEmpty() && _pushTo.isEmpty()) {
			log.warn("Cannot send email - no recipients");
			return;
		}
		
		// If we're in test mode, send back to the sender only
		if (SystemData.getBoolean("smtp.testMode")) {
			log.warn(String.format("STMP Test Mode enabled - sending to %s", _env.getFrom().getEmail()));
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

		// Loop through the SMTP recipients
		_env.addHeader("X-Golgotha-template", _ctx.getTemplate().getName());
		_env.addHeader("X-Golgotha-mass", String.valueOf(_msgTo.size() > 1));
		_msgTo.forEach(this::sendSMTP);
		_msgTo.clear();
		
		// Loop through the Push recipeints
		_pushTo.forEach(this::sendPush);
		_pushTo.clear();
	}
}