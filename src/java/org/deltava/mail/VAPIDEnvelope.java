// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.mail;

import java.net.URL;
import java.time.Instant;

import org.deltava.beans.NotificationEnvelope;
import org.deltava.beans.PushEndpoint;

/**
 * An envelope for VAPID messages.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class VAPIDEnvelope implements NotificationEnvelope<PushEndpoint> {
	
	private final PushEndpoint _ep;
	private final Instant _created = Instant.now();
	private Instant _expTime = _created.plusSeconds(3600);
	private String _jwt;
	private String _body;
	
	/**
	 * Creates the envelope.
	 * @param ep the PushEndpoint to send to 
	 */
	public VAPIDEnvelope(PushEndpoint ep) {
		super();
		_ep = ep;
	}
	
	/**
	 * Returns the audience used to generate the JWT for this message.
	 * @return the procotol/domain
	 */
	public String getAudience() {
		try {
			URL url = new URL(_ep.getURL());
			StringBuilder buf = new StringBuilder(url.getProtocol());
			buf.append("://").append(url.getHost());
			return buf.toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Returns the message body.
	 * @return the body
	 */
	public String getBody() {
		return _body;
	}
	
	@Override
	public Instant getCreatedOn() {
		return _created;
	}
	
	@Override
	public PushEndpoint getEndpoint() {
		return _ep;
	}
	
	@Override
	public Protocol getProtocol() {
		return Protocol.VAPID;
	}
	
	/**
	 * Returns the expiration time of this Envelope within the VAPID service.
	 * @return the expiration date/time
	 */
	public Instant getExpiryTime() {
		return _expTime;
	}
	
	/**
	 * Returns the JWT token.
	 * @return the JWT token, or null if not generated
	 */
	public String getToken() {
		return _jwt;
	}
	
	/**
	 * Updates the message body.
	 * @param body the body text
	 */
	public void setBody(String body) {
		_body = body;
	}
	
	/**
	 * Updates the JWT token used to authenticate this message.
	 * @param jwt the JWT token
	 */
	public void setToken(String jwt) {
		_jwt = jwt;
	}

	/**
	 * Updates the expiration time of this Envelope within the VAPID service. The expiration time must be non-null
	 * and after the creation time.
	 * @param dt the expiration date/time
	 */
	public void setExpirationTime(Instant dt) {
		if ((dt != null) && dt.isAfter(_created))
			_expTime = dt;
	}
}