// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

/**
 * An interface for notification envelopes. 
 * @author Luke
 * @version 10.0
 * @param <T> the destination address type
 * @since 10.0
 */

public interface NotificationEnvelope<T> extends java.io.Serializable, Cloneable, Comparable<NotificationEnvelope<?>> {
	
	/**
	 * An enumeration of notification protocols.
	 */
	public enum Protocol {
		SMTP, VAPID
	}

	/**
	 * Returns the destination address. 
	 * @return the address
	 */
	public T getEndpoint();
	
	/**
	 * Returns the envelope creation date.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn();
	
	/**
	 * Returns this envelope's notification protocol.
	 * @return the Protocol
	 */
	public Protocol getProtocol(); 

	@Override
	default int compareTo(NotificationEnvelope<?> me) {
		return getCreatedOn().compareTo(me.getCreatedOn());
	}
}