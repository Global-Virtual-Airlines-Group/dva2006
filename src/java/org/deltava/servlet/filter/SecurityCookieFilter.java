// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.deltava.beans.*;
import org.deltava.beans.system.IPBlock;
import org.deltava.crypt.*;
import org.deltava.security.*;

import static org.deltava.commands.HTTPContext.*;
import static org.deltava.commands.CommandContext.*;

import org.deltava.dao.*;
import org.gvagroup.jdbc.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A servlet filter to handle persistent authentication cookies.
 * @author Luke
 * @version 5.2
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
	@Override
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

	/*
	 * Helper method to return the value of a particular cookie.
	 */
	private static String getCookie(HttpServletRequest req, String name) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null) return null;
		for (int x = 0; x < cookies.length; x++) {
			Cookie c = cookies[x];
			if (c.getName().equals(name))
				return c.getValue();
		}

		return null;
	}

	/**
	 * Called by the servlet container on each request. Repopulates the session if a cookie is found.
	 * @param req the Servlet Request
	 * @param rsp the Servlet Response
	 * @param fc the Filter Chain
	 * @throws IOException if an I/O error occurs
	 * @throws ServletException if a general error occurs
	 */
	@Override
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
		HttpSession s = hreq.getSession(true);
		SecurityCookieData cData = (SecurityCookieData) s.getAttribute(AUTH_COOKIE_NAME);
		if (cData == null) {
			try {
				cData = SecurityCookieGenerator.readCookie(authCookie);
				if (SystemData.getBoolean("security.cookie.checkIP")) {
					if ((cData != null) && (!remoteAddr.equals(cData.getRemoteAddr())))
						throw new SecurityException(remoteAddr + " != " + cData.getRemoteAddr());
				}
				
				s.setAttribute(AUTH_COOKIE_NAME, cData);
			} catch (Exception e) {
				log.error("Error decrypting security cookie from " + req.getRemoteHost() + " using " + hreq.getHeader("user-agent") + " - " + e.getMessage());
				hrsp.addCookie(new Cookie(AUTH_COOKIE_NAME, ""));
				cData = null;
			}
		}
		
		// If the context has expired, invalidate the session
		Pilot p = (Pilot) s.getAttribute(USER_ATTR_NAME);
		if ((cData != null) && cData.isExpired()) {
			log.warn("Cookie for " + cData.getUserID() + " has expired");
			hrsp.addCookie(new Cookie(AUTH_COOKIE_NAME, ""));
			req.setAttribute("isExpired", Boolean.TRUE);
			s.invalidate();
			cData = null;
			p = null;
		} else if (cData != null) {
			long timeUntilExpiry = (cData.getExpiryDate() - System.currentTimeMillis()) / 1000;
			
			// Renew the cookie if it's about to expire
			if (timeUntilExpiry < 600) {
				cData.setExpiryDate(cData.getExpiryDate() + (1800 * 1000));
				String newCookie = SecurityCookieGenerator.getCookieData(cData);
				hrsp.addCookie(new Cookie(AUTH_COOKIE_NAME, newCookie));	
			}
		}

		// Validate the session/cookie data
		Connection con = null;
		try {
			String savedAddr = (cData == null) ? null : cData.getRemoteAddr();
			if (UserPool.isBlocked(p))
				throw new SecurityException(p.getName() + " is blocked");
			else if (hreq.isRequestedSessionIdFromURL())
				throw new SecurityException(req.getRemoteHost() + " attempting to create HTTP session via URL");
			else if ((savedAddr != null) && !remoteAddr.equals(savedAddr)) {
				con = _jdbcPool.getConnection();
				GetIPLocation ipdao = new GetIPLocation(con);
				IPBlock ipb = ipdao.get(savedAddr);
				if ((ipb == null) || (!ipb.contains(remoteAddr)))
					throw new SecurityException("HTTP Session is from " + savedAddr + ", request from " + remoteAddr);
			}
		} catch (Exception se) {
			log.warn(se.getMessage());
			hrsp.addCookie(new Cookie(AUTH_COOKIE_NAME, ""));
			req.setAttribute("servlet_error", se.getMessage());
			s.invalidate();
			cData = null;
		} finally {
			_jdbcPool.release(con);
		}
			
		// Load the user
		if ((p == null) && (cData != null)) {
			s.setAttribute(SCREENX_ATTR_NAME, Integer.valueOf(cData.getScreenX()));
			s.setAttribute(SCREENY_ATTR_NAME, Integer.valueOf(cData.getScreenY()));
			IPBlock addrInfo = null;
			
			// Load the person and the IP Address data
			try {
				con = _jdbcPool.getConnection();
				
				// Get the person
				GetPilot dao = new GetPilot(con);
				p = dao.get(StringUtils.parseHex(cData.getUserID()));
				
				// Populate online/ACARS legs
				if (p != null) {
					GetFlightReports frdao = new GetFlightReports(con);
					frdao.getOnlineTotals(p, SystemData.get("airline.db"));
					
					// Load the IP address data
					GetIPLocation ipdao = new GetIPLocation(con);
					addrInfo = ipdao.get(remoteAddr);
				} else
					log.error("Unknown Pilot - " + cData.getUserID());
			} catch (DAOException de) {
				log.error("Error loading " + cData.getUserID() + " - " + de.getMessage(), de);
			} catch (org.gvagroup.jdbc.ConnectionPoolException cpe) {
				log.error(cpe.getMessage());
			} finally {
				_jdbcPool.release(con);
			}
			
			// Make sure that the pilot is still active
			if (p != null) {
				try {
					if (p.getStatus() == Pilot.ACTIVE) {
						String userAgent = hreq.getHeader("user-agent");
						s.setAttribute(USERAGENT_ATTR_NAME, userAgent);
						s.setAttribute(USER_ATTR_NAME, p);
						log.info("Restored " + p.getName() + " from Security Cookie");
							
						// Check if we are a superUser impersonating someone
						Pilot su = (Pilot) s.getAttribute(SU_ATTR_NAME);
						UserPool.add((su != null) ? su : p, s.getId(), addrInfo, userAgent);
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
	@Override
	public void destroy() {
		log.info("Stopped");
	}
}