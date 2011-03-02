// Copyright 2005, 2006, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.Person;

/**
 * A security context that allows user swapping to test access by multiple
 * users to a single object.
 * @author Luke
 * @version 3.6
 * @since 3.6
 */

public class MultiUserSecurityContext implements SecurityContext {

	private Person _usr;
	private HttpServletRequest _req;

	/**
	 * Creates the context.
	 * @param ctx the existing SecurityContext
	 */
	public MultiUserSecurityContext(SecurityContext ctx) {
		super();
		setUser(ctx.getUser());
		_req = ctx.getRequest();
	}

	public Person getUser() {
		return _usr;
	}

	public HttpServletRequest getRequest() {
		return _req;
	}

	public boolean isAuthenticated() {
		return (_usr != null);
	}

	public Collection<String> getRoles() {
		return isAuthenticated() ? getUser().getRoles() : new HashSet<String>();
	}

	public boolean isUserInRole(String roleName) {
		return _req.isUserInRole(roleName);
	}

	/**
	 * Overrides the user.
	 * @param usr the User object
	 */
	public void setUser(Person usr) {
		_usr = usr;
	}
}