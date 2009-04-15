// Copyright 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.lifecycle;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.system.IPAddressInfo;

import org.deltava.security.UserPool;

import static org.deltava.commands.HTTPContext.*;

/**
 * An HTTP session listener to track serialization of User sessions.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class UserStartupListener implements java.io.Serializable, HttpSessionActivationListener {

	private transient static final Logger log = Logger.getLogger(UserStartupListener.class);

	/**
	 * Package-private constructor.
	 */
	UserStartupListener() {
		super();
	}

	/**
	 * Called before serialization of an HTTP session.
	 * @param e the lifecycle event
	 */
	public void sessionWillPassivate(HttpSessionEvent e) {
		HttpSession s = e.getSession();

		try {
			// Get the User
			Person p = (Person) s.getAttribute(USER_ATTR_NAME);
			if (p == null)
				return;

			// Remove the user
			UserPool.remove(p, s.getId());
			if (log.isDebugEnabled())
				log.debug("Serializing Session " + s.getId());
		} catch (IllegalStateException ise) {
			System.err.println("Attempting to save invalid Session");
		}
	}

	/**
	 * Called on the activation of a serialized HTTP session.
	 * @param e the lifecycle event
	 */
	public void sessionDidActivate(HttpSessionEvent e) {
		HttpSession s = e.getSession();
		try {
			IPAddressInfo addrInfo = (IPAddressInfo) s.getAttribute(ADDRINFO_ATTR_NAME);
			Person p = (Person) s.getAttribute(USER_ATTR_NAME);
			if (p == null)
				return;

			// Add the user to the User pool
			if (p instanceof Pilot)
				UserPool.add((Pilot) p, s.getId(), addrInfo, "Unknown");
		} catch (IllegalStateException ise) {
			System.err.println("Attempting to restore invalid Session");
		}
	}
}