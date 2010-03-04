// Copyright 2005, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.redirect;

import java.util.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

/**
 * A utility class to save and restore servlet request state.
 * @author Luke
 * @version 3.0
 * @since 1.0
 */

public class RequestStateHelper {

	private static final Logger log = Logger.getLogger(RequestStateHelper.class);

	/**
	 * The name of the HTTP session attribute used to save servlet request state.
	 */
	public static final String STATE_ATTR_NAME = "$requestState$";

	/**
	 * Restores the current servlet request state. This will load the request state from the current HTTP session and
	 * populate the request attributes. <i>Existing request attributes will be overwritten. </i>
	 * @param req the current servlet request
	 * @return the URL to forward to
	 * @throws IllegalStateException if there is no current HTTP session, or the request state attribute is not present
	 * in the session
	 */
	public static String restore(HttpServletRequest req) {

		// Get the current HTTP session
		HttpSession s = req.getSession(false);
		if (s == null)
			throw new IllegalStateException("No existing HTTP session");

		// Get the request state attribute
		RequestContent rc = (RequestContent) s.getAttribute(STATE_ATTR_NAME);
		if (rc == null)
			throw new IllegalStateException("Request state not found in HTTP session");

		// Restore the request attributes
		for (Iterator<String> i = rc.getAttributeNames().iterator(); i.hasNext();) {
			String attrName = i.next();
			req.setAttribute(attrName, rc.getAttribute(attrName));
			if (log.isDebugEnabled())
				log.debug("Restoring attribute " + attrName);
		}

		// Return the URL to forward to
		return rc.getURL();
	}

	/**
	 * Saves the current servlet request state to the current HTTP session.
	 * @param req the current servlet request
	 * @param url the URL to forward to after the servlet request state is restored
	 * @throws IllegalStateException if an HTTP session does not currently exist
	 */
	public static void save(HttpServletRequest req, String url) {

		RequestContent rc = new RequestContent(url);

		// Save the attributes
		Enumeration<?> attrs = req.getAttributeNames();
		while (attrs.hasMoreElements()) {
			String attrName = (String) attrs.nextElement();
			Object o = req.getAttribute(attrName);
			if (!(o instanceof Cookie)) {
				rc.setAttribute(attrName, o);
				if (log.isDebugEnabled())
					log.debug("Saving attribute " + attrName);
			}
		}

		// Get the current HTTP session
		HttpSession s = req.getSession(false);
		if (s == null)
			throw new IllegalStateException("No existing HTTP session");

		// Save in the session
		s.setAttribute(STATE_ATTR_NAME, rc);
	}

	/**
	 * Helper method to remove the request state object from the session, if found.
	 * @param req the current servlet request
	 */
	public static void clear(HttpServletRequest req) {
		if (req.isRequestedSessionIdValid()) {
			HttpSession s = req.getSession(false);
			if ((s != null) && (s.getAttribute(STATE_ATTR_NAME) != null)) {
				s.removeAttribute(STATE_ATTR_NAME);
				if (log.isDebugEnabled())
					log.debug("Clearing saved request state");
			}
		}
	}
}