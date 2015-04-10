// Copyright 2005, 2007, 2010, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.deltava.beans.Person;
import org.deltava.beans.servlet.ServletScoreboard;

import org.gvagroup.jdbc.ConnectionPool;
import org.deltava.security.SecurityContext;

import org.deltava.util.ControllerException;
import org.deltava.util.system.SystemData;

/**
 * A class storing common servlet helper methods.
 * @author Luke
 * @version 6.0
 * @since 1.0
 */

public abstract class GenericServlet extends HttpServlet {

	protected class ServletSecurityContext implements SecurityContext {

		private final List<String> ANONYMOUS_ROLES = Arrays.asList("Anonymous");

		private final HttpServletRequest _req;

		public ServletSecurityContext(HttpServletRequest req) {
			super();
			_req = req;
		}

		@Override
		public Person getUser() {
			return (Person) _req.getUserPrincipal();
		}

		@Override
		public boolean isAuthenticated() {
			return (getUser() != null);
		}

		@Override
		public Collection<String> getRoles() {
			return isAuthenticated() ? getUser().getRoles() : ANONYMOUS_ROLES;
		}

		@Override
		public boolean isUserInRole(String roleName) {
			if ("*".equals(roleName))
				return true;

			return isAuthenticated() ? getUser().isInRole(roleName) : ANONYMOUS_ROLES.contains(roleName);
		}

		@Override
		public HttpServletRequest getRequest() {
			return _req;
		}
	}

	protected class NotFoundException extends ControllerException {
		public NotFoundException(String msg) {
			super(msg);
			setWarning(true);
		}
	}

	/**
	 * A helper method to get the JDBC Connection Pool.
	 * @return the JDBC Connection Pool
	 */
	protected static ConnectionPool getConnectionPool() {
		return (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
	}

	/**
	 * Process the request. This is a stub method that merely adds hooks to {@link ServletScoreboard}.
	 * @param req the request
	 * @param rsp the response
	 * @throws ServletException if a Servlet error occurs
	 * @throws IOException if a network error occurs
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		ServletScoreboard.add(req);
		try {
			super.service(req, rsp);
		} finally {
			ServletScoreboard.complete();
		}
	}
}