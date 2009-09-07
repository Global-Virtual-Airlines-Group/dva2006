// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.system.IPAddressInfo;

import org.deltava.crypt.*;
import org.deltava.security.*;

import static org.deltava.commands.HTTPContext.*;
import static org.deltava.commands.CommandContext.*;

import org.deltava.dao.*;
import org.deltava.jdbc.ConnectionPool;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A servlet filter to handle persistent authentication cookies.
 * @author Luke
 * @version 2.6
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
		String remoteAddr = req.getRemoteAddr();

		// Check for the authentication cookie
		String authCookie = getCookie(hreq, AUTH_COOKIE_NAME);
		if (StringUtils.isEmpty(authCookie)) {
			fc.doFilter(req, rsp);
			return;
		}

		// Decrypt the cookie
		SecurityCookieData cData = null;
		try {
			cData = SecurityCookieGenerator.readCookie(authCookie);
			if (SystemData.getBoolean("security.cookie.checkIP")) {
				if ((cData != null) && (!remoteAddr.equals(cData.getRemoteAddr())))
					throw new SecurityException(remoteAddr + " != " + cData.getRemoteAddr());
			}
		} catch (Exception e) {
			log.error("Error decrypting security cookie from " + req.getRemoteHost() + " using " + hreq.getHeader("user-agent") + " - " + e.getMessage());
			hrsp.addCookie(new Cookie(AUTH_COOKIE_NAME, ""));
			cData = null;
		}

		// Validate the session/cookie data
		HttpSession s = hreq.getSession(true);
		Pilot p = (Pilot) s.getAttribute(USER_ATTR_NAME);
		try {
			String savedAddr = (String) s.getAttribute(ADDR_ATTR_NAME);
			if (UserPool.isBlocked(p))
				throw new SecurityException(p.getName() + " is blocked");
			else if (hreq.isRequestedSessionIdFromURL())
				throw new SecurityException(req.getRemoteHost() + " attempting to create HTTP session via URL");
			else if ((savedAddr != null) && !remoteAddr.equals(savedAddr))
				throw new SecurityException("HTTP Session is from " + savedAddr + ", request from " + remoteAddr);
			else if (savedAddr == null)
				s.setAttribute(ADDR_ATTR_NAME, remoteAddr);
		} catch (SecurityException se) {
			log.warn(se.getMessage());
			hrsp.addCookie(new Cookie(AUTH_COOKIE_NAME, ""));
			s.invalidate();
			p = null;
			cData = null;
		}
			
		// Load the user
		if ((p == null) && (cData != null)) {
			s.setAttribute(SCREENX_ATTR_NAME, new Integer(cData.getScreenX()));
			s.setAttribute(SCREENY_ATTR_NAME, new Integer(cData.getScreenY()));
			IPAddressInfo addrInfo = null;
			
			// Load the person and the IP Address data
			Connection con = null;
			try {
				con = _jdbcPool.getConnection();
				
				// Get the person
				GetPilotDirectory dao = new GetPilotDirectory(con);
				p = dao.getFromDirectory(cData.getUserID());
				
				// Populate online/ACARS legs
				if (p != null) {
					GetFlightReports frdao = new GetFlightReports(con);
					frdao.getOnlineTotals(p, SystemData.get("airline.db"));
				} else
					log.error("Unknown Pilot - " + cData.getUserID());

				// Load the IP address data
				GetIPLocation ipdao = new GetIPLocation(con);
				addrInfo = ipdao.get(remoteAddr);
			} catch (DAOException de) {
				log.error("Error loading " + cData.getUserID() + " - " + de.getMessage(), de);
			} finally {
				_jdbcPool.release(con);
			}
			
			// Make sure that the pilot is still active
			if (p != null) {
				try {
					if (p.getStatus() == Pilot.ACTIVE) {
						if (authenticate(p, cData.getPassword())) {
							s.setAttribute(USER_ATTR_NAME, p);
							log.info("Restored " + p.getName() + " from Security Cookie");
							
							// Check if we are a superUser impersonating someone
							Pilot su = (Pilot) s.getAttribute(SU_ATTR_NAME);
							UserPool.add((su != null) ? su : p, s.getId(), addrInfo, hreq.getHeader("user-agent"));
						} else 
							throw new SecurityException("Cannot re-authenticate " + p.getName());
					} else
						throw new SecurityException(p.getName() + " status = " + p.getStatusName());
				} catch (SecurityException se) {
					log.error(se.getMessage());
					hrsp.addCookie(new Cookie(AUTH_COOKIE_NAME, ""));
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