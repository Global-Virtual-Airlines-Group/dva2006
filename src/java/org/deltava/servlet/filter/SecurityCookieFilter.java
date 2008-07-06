// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.crypt.*;
import org.deltava.security.*;

import org.deltava.commands.CommandContext;

import org.deltava.dao.*;
import org.deltava.jdbc.ConnectionPool;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A servlet filter to handle persistent authentication cookies.
 * @author Luke
 * @version 2.2
 * @since 1.0
 * @see SecurityCookieData
 * @see SecurityCookieGenerator
 */

public class SecurityCookieFilter implements Filter {

	private static final Logger log = Logger.getLogger(SecurityCookieFilter.class);
	
	private ConnectionPool _jdbcPool;
	
	/**
	 * Called by the servlet container when the filter is started. Logs a message and saves the servlet context.
	 * @param cfg the Filter Configuration
	 */
	public void init(FilterConfig cfg) throws ServletException {
		try {
			SecretKeyEncryptor enc = new DESEncryptor(SystemData.get("security.desKey"));
			SecurityCookieGenerator.init(enc);
		} catch (NullPointerException npe) {
			throw new ServletException("No 3DES Key provided");
		} catch (CryptoException ce) {
			throw new ServletException("Error Initializing Security Cookie key", ce);
		}
		
		// Initialize the JDBC Connection pool
		_jdbcPool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
		log.info("Started");
	}

	/**
	 * Helper method to return the value of a particular cookie.
	 */
	private String getCookie(HttpServletRequest req, String name) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null)
			return null;

		for (int x = 0; x < cookies.length; x++) {
			Cookie c = cookies[x];
			if (c.getName().equals(name))
				return c.getValue();
		}

		return null;
	}

	/**
	 * Helper method to load a Person from the database.
	 */
	private Pilot loadPersonFromDatabase(String dN) {

		Connection con = null;
		Pilot p = null;
		try {
			con = _jdbcPool.getConnection(true);

			// Get the person
			GetPilotDirectory dao = new GetPilotDirectory(con);
			p = dao.getFromDirectory(dN);
			
			// Populate online/ACARS legs
			if (p != null) {
				GetFlightReports frdao = new GetFlightReports(con);
				frdao.getOnlineTotals(p, SystemData.get("airline.db"));
			} else
				log.error("Unknown Pilot - " + dN);
		} catch (DAOException de) {
			log.error("Error loading " + dN + " - " + de.getMessage(), de);
		} finally {
			_jdbcPool.release(con);
		}

		// Return the person
		return p;
	}
	
	/**
	 * Helper method to revalidate a user's credentials.
	 */
	private boolean authenticate(Pilot p, String pwd) {
		
		// Get the authenticator and validate the password
		Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
		Connection con = null; boolean isOK = false;
		try {
			if (auth instanceof SQLAuthenticator) {
				SQLAuthenticator sqlAuth = (SQLAuthenticator) auth;
				sqlAuth.setConnection(con);
				sqlAuth.authenticate(p, pwd);
				sqlAuth.clearConnection();
			} else
				auth.authenticate(p, pwd);
			
			isOK = true;
		} catch (SecurityException se) {
			log.error("Cannot reauthenticate " + p.getName());
		} finally {
			_jdbcPool.release(con);
		}
		
		return isOK;
	}
	
	/**
	 * Called by the servlet container on each request. Repopulates the session if a cookie is found.
	 * @param req the Servlet Request
	 * @param rsp the Servlet Response
	 * @param fc the Filter Chain
	 * @throws IOException if an I/O error occurs
	 * @throws ServletException if a general error occurs
	 */
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain fc) throws IOException, ServletException {

		// Cast the request/response since we are doing stuff with them
		HttpServletRequest hreq = (HttpServletRequest) req;
		HttpServletResponse hrsp = (HttpServletResponse) rsp;

		// Check for the authentication cookie
		String authCookie = getCookie(hreq, CommandContext.AUTH_COOKIE_NAME);
		if (StringUtils.isEmpty(authCookie)) {
			fc.doFilter(req, rsp);
			return;
		}

		// Decrypt the cookie
		SecurityCookieData cData = null;
		try {
			cData = SecurityCookieGenerator.readCookie(authCookie);
		} catch (Exception e) {
			log.error("Error decrypting security cookie from " + req.getRemoteHost() + " - " + e.getMessage());
			hrsp.addCookie(new Cookie(CommandContext.AUTH_COOKIE_NAME, ""));
		}

		// Get the user attribute and/ or reauthenticate the user
		HttpSession s = hreq.getSession(true);
		Pilot p = (Pilot) s.getAttribute(CommandContext.USER_ATTR_NAME);
		if (UserPool.isBlocked(p)) {
			hrsp.addCookie(new Cookie(CommandContext.AUTH_COOKIE_NAME, ""));
			p = null;
		} else if (hreq.isRequestedSessionIdFromURL()) {
			log.warn(req.getRemoteHost() + " attempting to create HTTP session via URL");
			hrsp.addCookie(new Cookie(CommandContext.AUTH_COOKIE_NAME, ""));
			s.invalidate();
			p = null;
		}

		// Load the user
		if ((p == null) && (cData != null)) {
			s.setAttribute(CommandContext.SCREENX_ATTR_NAME, new Integer(cData.getScreenX()));
			s.setAttribute(CommandContext.SCREENY_ATTR_NAME, new Integer(cData.getScreenY()));
			p = loadPersonFromDatabase(cData.getUserID());

			// Make sure that the pilot is still active
			if (p != null) {
				if (p.getStatus() == Pilot.ACTIVE) {
					if (authenticate(p, cData.getPassword())) {
						s.setAttribute(CommandContext.USER_ATTR_NAME, p);
						log.info("Restored " + p.getName() + " from Security Cookie");
						
						// Check if we are a superUser impersonating someone
						Person su = (Pilot) s.getAttribute(CommandContext.SU_ATTR_NAME);
						UserPool.add((su != null) ? su : p, s.getId());
					} else {
						log.error("Cannot re-authenticate " + p.getName());
						hrsp.addCookie(new Cookie(CommandContext.AUTH_COOKIE_NAME, ""));
					}
				} else {
					log.warn(p.getName() + " status = " + p.getStatusName());
					hrsp.addCookie(new Cookie(CommandContext.AUTH_COOKIE_NAME, ""));
					s.invalidate();
				}
			}
		}

		// Invoke the next filter in the chain
		fc.doFilter(req, rsp);
	}

	/**
	 * Called by the servlet container when the filter is stopped. Logs a message.
	 */
	public void destroy() {
		log.info("Stopped");
	}
}