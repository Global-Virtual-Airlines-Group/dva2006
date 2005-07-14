// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.mail;

import org.deltava.util.system.SystemData;

/**
 * An e-mail message formatter.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class Message {

	private String _msgBody;
	private MessageContext _ctx;

	/**
	 * Creates a new message using a particular Message Context.
	 * @param ctx the message context
	 */
	public Message(MessageContext ctx) {
		super();
		_ctx = ctx;
	}

	/**
	 * Returns the message subject.
	 * @return the subject prepended by the Airline Name
	 */
	public String getSubject() {
		return SystemData.get("airline.name") + " " + _ctx.getTemplate().getSubject();
	}

	/**
	 * Returns the formatted message body text.
	 * @return the body text, or null if {@link Message#format()} has not been called yet
	 */
	public String getBody() {
		return _msgBody;
	}

	/**
	 * Formats the message by replacing arguments in the message template with values from the message context.
	 */
	public void format() {

		// Check that the message context has been set
		if (_ctx == null)
			throw new IllegalStateException("Message Context not set");
		
		// If there's no template, then abort
		if (_ctx.getTemplate() == null)
			return;

		// Load the Message template
		StringBuffer buf = new StringBuffer(_ctx.getTemplate().getBody());

		// Parse the message template with data from the MessageContext
		int spos = buf.indexOf("${");
		while (spos != -1) {
			int epos = buf.indexOf("}", spos);

			// Only format if the end token can be found
			if (epos > spos) {
				String token = buf.substring(spos + 2, epos);
				buf.replace(spos, epos + 1, _ctx.execute(token));
				spos = buf.indexOf("${");
			} else {
				spos = buf.indexOf("${", spos);
			}
		}
		
		// Save the message body
		_msgBody = buf.toString();
	}
}