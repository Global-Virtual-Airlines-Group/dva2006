// Copyright 2005, 2007, 2010, 2012, 2014, 2015, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.sql.Connection;
import java.io.IOException;
import java.util.Base64;
import java.util.StringTokenizer;

import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.deltava.beans.Pilot;
import org.deltava.dao.*;
import org.gvagroup.jdbc.*;
import org.deltava.security.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A servlet that supports basic HTTP authentication.
 * @author Luke
 * @version 8.2
 * @since 1.0
 */

abstract class BasicAuthServlet extends GenericServlet {

	private static final Logger log = Logger.getLogger(BasicAuthServlet.class);

	/**
	 * Authenticates the current web user.
	 * @param req the current HTTP servlet request
	 * @return the authenticated Pilot's databse record, or null if not logged in
	 */
	protected static Pilot authenticate(HttpServletRequest req) {

		// Check for Authentication header
		String authHdr = req.getHeader("Authorization");
		if ((authHdr == null) || (!authHdr.toUpperCase().startsWith("BASIC ")))
			return null;

		// Get encoded username/password, and decode them
		String userPwd = new String(Base64.getDecoder().decode(authHdr.substring(6)));
		StringTokenizer tkns = new StringTokenizer(userPwd, ":");
		if (tkns.countTokens() != 2)
			return null;

		// Get the JDBC Connection Pool
		ConnectionPool pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);

		Connection con = null;
		Pilot p = null;
		try {
			con = pool.getConnection();

			// Get the DAO and the directory name for this user
			GetPilotDirectory dao = new GetPilotDirectory(con);
			UserID id = new UserID(tkns.nextToken()); Pilot usr = null;
			if (id.hasAirlineCode())
				usr = dao.getByCode(id.toString());
			else
				usr = dao.get(id.getUserID());
			
			if (usr == null)
				throw new SecurityException("Unknown User ID - " + id);
			
			// Authenticate the user
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			if (auth instanceof SQLAuthenticator) {
				try (SQLAuthenticator sqlAuth = (SQLAuthenticator) auth) {
					sqlAuth.setConnection(con);
					sqlAuth.authenticate(usr, tkns.nextToken());
				}
			} else
				auth.authenticate(usr, tkns.nextToken());
			
			p = usr;
		} catch (SecurityException se) {
			log.warn("Authentication failure - " + se.getMessage());
		} catch (DAOException de) {
			log.warn("Data load failure - " + de.getMessage());
		} catch (ConnectionPoolException cpe) {
			log.warn("Connection pool error - " + cpe.getMessage());
		} finally {
			pool.release(con);
		}

		return p;
	}
	
	/**
	 * Sets the response headers for a basic authentication challenge.
	 * @param rsp the HTTP servlet response
	 * @param realm the realm name to present to the browser
	 * @throws IOException if a network error occurs
	 */
	protected static void challenge(HttpServletResponse rsp, String realm) throws IOException {
		rsp.setHeader("WWW-Authenticate", "Basic realm=" + realm);
		rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
	}
}