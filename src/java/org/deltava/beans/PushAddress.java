// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.Collection;

/**
 * An interface to mark objects that can be used as the recipient of a Push Notification message. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public interface PushAddress {

	/**
	 * Returns the user's Push Notification endpoints.
	 * @return a Collection of PushEndpoint beans
	 */
	public Collection<PushEndpoint> getPushEndpoints();
}