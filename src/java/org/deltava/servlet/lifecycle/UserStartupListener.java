// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.lifecycle;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Person;

import org.deltava.commands.CommandContext;
import org.deltava.security.UserPool;

/**
 * An HTTP session listener to track serialization of User sessions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserStartupListener implements java.io.Serializable, HttpSessionActivationListener {

	private static final Logger log = Logger.getLogger(UserStartupListener.class);

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

		// Get the User
		Person p = (Person) s.getAttribute(CommandContext.USER_ATTR_NAME);
		if (p == null)
			return;
		
		// Remove the user
		UserPool.remove(p, s.getId());
		if (log.isDebugEnabled())
			log.debug("Serializing Session " + s.getId());
	}

	/**
	 * Called on the activation of a serialized HTTP session.
	 * @param e the lifecycle event
	 */
	public void sessionDidActivate(HttpSessionEvent e) {
		HttpSession s = e.getSession();
		
		// Get the User
		Person p = (Person) s.getAttribute(CommandContext.USER_ATTR_NAME);
		if (p == null)
			return;
		
		// Add the user to the User pool
		UserPool.add(p, s.getId());
	}
}