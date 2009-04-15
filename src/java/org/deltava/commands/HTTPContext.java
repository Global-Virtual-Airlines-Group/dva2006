// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;

import javax.servlet.http.*;

import org.deltava.beans.*;

import org.deltava.jdbc.ConnectionContext;
import org.deltava.security.SecurityContext;

/**
 * An abstract class to share command data between different HTTP command contexts.
 * @author Luke
 * @version 2.5
 * @since 2.4
 */

public abstract class HTTPContext extends ConnectionContext implements SecurityContext {

	private static final Collection<String> ANONYMOUS_ROLES = Collections.singleton("Anonymous");

	public static final String ADDRINFO_ATTR_NAME = "ipAddrInfo";
	public static final String USER_ATTR_NAME = "authUser";
	public static final String SU_ATTR_NAME = "superUser";

	private HttpServletRequest _req;
	private HttpServletResponse _rsp;

	private Person _usr;

	/**
	 * Creates a new Command context from an HTTP Servlet Request/Resposne pair.
	 * @param req the Servlet Request
	 * @param rsp the Servlet Response
	 */
	public HTTPContext(HttpServletRequest req, HttpServletResponse rsp) {
		super();
		_req = req;
		_rsp = rsp;
	}

	/**
	 * Returns the current HTTP session.
	 * @return the HTTP session, null if none present or invalid
	 */
	public HttpSession getSession() {
		return _req.getSession(false);
	}

	/**
	 * Returns the current HTTP Servlet Request.
	 * @return the Servlet Request
	 */
	public HttpServletRequest getRequest() {
		return _req;
	}

	/**
	 * Returns the current HTTP Servlet Response.
	 * @return the Servlet Response
	 */
	public HttpServletResponse getResponse() {
		return _rsp;
	}

	/**
	 * Returns the authenticated user object.
	 * @return the User object, or null if anonymous
	 * @see HTTPContext#isAuthenticated()
	 * @see HTTPContext#isUserInRole(String)
	 */
	public Person getUser() {
		return (_usr == null) ? (Person) _req.getUserPrincipal() : _usr;
	}

	/**
	 * Updates the User executing this operation. This method has no
	 * effect if a user object is already present in the request.
	 * @param p the User object, or null if anonymous
	 */
	public void setUser(Person p) {
		if (_req.getUserPrincipal() == null)
			_usr = p;
	}

	/**
	 * Returns if this command is being invoked by an authenticated user.
	 * @return TRUE if the user is logged in, FALSE otherwise
	 * @see CommandContext#getUser()
	 * @see CommandContext#getRoles()
	 */
	public boolean isAuthenticated() {
		return (getUser() != null);
	}

	/**
	 * Returns whether an Administrator is impersonating another user.
	 * @return TRUE if superuser mode is on, otherwise FALSE
	 */
	public boolean isSuperUser() {
		HttpSession s = _req.getSession(false);
		return (s == null) ? false : (s.getAttribute(SU_ATTR_NAME) instanceof Person);
	}

	/**
	 * Returns the list of roles for the user. This will return a List with a single element (&quot;Anonymous&quot;) if
	 * the user is not currently authenticated
	 * @return a Collection of role names
	 * @see CommandContext#isAuthenticated()
	 * @see CommandContext#getUser()
	 */
	public Collection<String> getRoles() {
		return isAuthenticated() ? getUser().getRoles() : ANONYMOUS_ROLES;
	}

	/**
	 * Returns if the currently logged in user is a member of a particular role. Unless a specific user has been injected, this 
	 * method delegates the call to the underlying HTTP servlet request's {@link HttpServletRequest#isUserInRole(String)}
	 * method, which may be overriden by a custom request handler.
	 * @param roleName the role name
	 * @return TRUE if the user is a member of the specified role, otherwise FALSE
	 */
	public boolean isUserInRole(String roleName) {
		if (_usr == null)
			return _req.isUserInRole(roleName);
		else if (isAuthenticated())
			return _usr.isInRole(roleName);

		return ("*".equals(roleName) || ANONYMOUS_ROLES.contains(roleName));
	}

	/**
	 * Returns the value of a request parameter.
	 * @param pName the parameter name
	 * @return the parameter value, or null if not present
	 */
	public String getParameter(String pName) {
		return _req.getParameter(pName);
	}

	/**
	 * Returns the values of a collection of request parameters.
	 * @param pName the parameter name
	 * @return a Collection parameter values, or null if not present
	 */
	public Collection<String> getParameters(String pName) {
		String[] pValues = _req.getParameterValues(pName);
		return (pValues == null) ? null : new ArrayList<String>(Arrays.asList(pValues));
	}

	/**
	 * Returns the value of an uploaded file object.
	 * @param name the file name
	 * @return the file data, or null if not found
	 */
	public FileUpload getFile(String name) {
		return (FileUpload) _req.getAttribute("FILE$" + name);
	}

	/**
	 * Returns an HTTP cookie.
	 * @param name the cookie name
	 * @return the cookie, or null if not found
	 */
	public Cookie getCookie(String name) {
		return (Cookie) _req.getAttribute("COOKIE$" + name);
	}

	/**
	 * Sets an attribute in a particular context.
	 * @param name the name of the attribute
	 * @param value the attribute value
	 * @param scope the scope (application, session or request)
	 * @see Command#REQUEST
	 * @see Command#SESSION
	 */
	public void setAttribute(String name, Object value, Command.Scope scope) {
		if (scope == Command.Scope.SES) {
			HttpSession s = _req.getSession(true);
			s.setAttribute(name, value);
		} else
			_req.setAttribute(name, value);
	}

	/**
	 * Returns one of the special command parameters.
	 * @param prmType the Parameter type
	 * @param defaultValue the value to return if parameter not specified in the request
	 * @return the parameter value
	 * @see Command#ID
	 * @see Command#OPERATION
	 */
	public Object getCmdParameter(int prmType, Object defaultValue) {
		switch (prmType) {
		case Command.OPERATION:
			String cmdOp = _req.getParameter("op");
			return (cmdOp == null) ? defaultValue : cmdOp;

		default:
		case Command.ID:
			String cmdID = _req.getParameter("id");
			if (cmdID == null) {
				return defaultValue;
			} else if (cmdID.startsWith("0x")) {
				try {
					return new Integer(Integer.parseInt(cmdID.substring(2), 16));
				} catch (NumberFormatException nfe) {
					return cmdID;
				}
			} else {
				return cmdID;
			}
		}
	}
}