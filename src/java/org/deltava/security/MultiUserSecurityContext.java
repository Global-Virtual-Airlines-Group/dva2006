// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.Pilot;

/**
 * A security context that allows user swapping to test access by multiple users to a single object.
 * @author Luke
 * @version 8.6
 * @since 3.6
 */

public class MultiUserSecurityContext implements SecurityContext {

	private Pilot _usr;
	private final HttpServletRequest _req;

	/**
	 * Creates the context.
	 * @param ctx the existing SecurityContext
	 */
	public MultiUserSecurityContext(SecurityContext ctx) {
		super();
		setUser(ctx.getUser());
		_req = ctx.getRequest();
	}

	@Override
	public Pilot getUser() {
		return _usr;
	}

	@Override
	public HttpServletRequest getRequest() {
		return _req;
	}

	@Override
	public boolean isAuthenticated() {
		return (_usr != null);
	}

	@Override
	public Collection<String> getRoles() {
		return isAuthenticated() ? _usr.getRoles() : Collections.emptySet();
	}

	@Override
	public boolean isUserInRole(String roleName) {
		return _req.isUserInRole(roleName);
	}

	/**
	 * Overrides the user.
	 * @param usr the User object
	 */
	public void setUser(Pilot usr) {
		_usr = usr;
	}
}