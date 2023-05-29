// Copyright 2006, 2008, 2009, 2011, 2012, 2015, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.lifecycle;

import javax.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.system.IPBlock;

import org.deltava.security.UserPool;

import static org.deltava.commands.HTTPContext.*;

/**
 * An HTTP session listener to track serialization of User sessions.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class UserStartupListener implements java.io.Serializable, HttpSessionActivationListener {

	private transient static final Logger log = LogManager.getLogger(UserStartupListener.class);
	
	static transient final UserStartupListener INSTANCE = new UserStartupListener();

	// singleton
	private UserStartupListener() {
		super();
	}

	/**
	 * Called before serialization of an HTTP session.
	 * @param e the lifecycle event
	 */
	@Override
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
			log.error("Attempting to save invalid Session");
		}
	}

	/**
	 * Called on the activation of a serialized HTTP session.
	 * @param e the lifecycle event
	 */
	@Override
	public void sessionDidActivate(HttpSessionEvent e) {
		HttpSession s = e.getSession();
		try {
			String userAgent = (String) s.getAttribute(USERAGENT_ATTR_NAME);
			IPBlock addrInfo = (IPBlock) s.getAttribute(ADDRINFO_ATTR_NAME);
			Person p = (Person) s.getAttribute(USER_ATTR_NAME);
			if (p == null)
				return;

			// Add the user to the User pool
			if (p instanceof Pilot)
				UserPool.add((Pilot) p, s.getId(), addrInfo, (userAgent == null) ?  "Unknown" : userAgent);
		} catch (IllegalStateException ise) {
			log.error("Attempting to restore invalid Session");
		}
	}
	
	@Override
	public String toString() {
		return getClass().getName();
	}
}