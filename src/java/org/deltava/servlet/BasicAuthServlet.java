// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.servlet;

import java.sql.Connection;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;

import org.deltava.dao.DAOException;
import org.deltava.dao.GetPilotDirectory;
import org.deltava.jdbc.ConnectionPool;

import org.deltava.security.Authenticator;

import org.deltava.util.Base64;
import org.deltava.util.system.SystemData;

/**
 * A servlet that supports basic authentication.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class BasicAuthServlet extends GenericServlet {

	private static final Logger log = Logger.getLogger(BasicAuthServlet.class);

	/**
	 * Authenticates the current web user.
	 * @param req the current HTTP servlet request
	 * @return the authenticated Pilot's databse record, or null if not logged in
	 */
	protected Pilot authenticate(HttpServletRequest req) {

		// Check for Authentication header
		String authHdr = req.getHeader("Authorization");
		if ((authHdr == null) || (!authHdr.toUpperCase().startsWith("BASIC ")))
			return null;

		// Get encoded username/password, and decode them
		String userPwd = Base64.decodeString(authHdr.substring(6));
		StringTokenizer tkns = new StringTokenizer(userPwd, ":");
		if (tkns.countTokens() != 2)
			return null;

		// Get the JDBC Connection Pool
		ConnectionPool pool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);

		Connection con = null;
		Pilot p = null;
		try {
			con = pool.getConnection(true);

			// Get the DAO and the directory name for this user
			GetPilotDirectory dao = new GetPilotDirectory(con);
			String userID = tkns.nextToken();
			String dN = dao.getDirectoryName(userID);
			if (dN == null)
				throw new SecurityException("Unknown User ID - " + userID);

			// Authenticate the user
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			auth.authenticate(dN, tkns.nextToken());

			// If we got this far, load the user data
			p = dao.getFromDirectory(dN);
		} catch (SecurityException se) {
			log.warn("Authentication failure - " + se.getMessage());
		} catch (DAOException de) {
			log.warn("Data load failure - " + de.getMessage());
		} finally {
			pool.release(con);
		}

		// Return the Pilot data
		return p;
	}
	
	/**
	 * Sets the response headers for a basic authentication challenge.
	 * @param rsp the HTTP servlet response
	 * @param realm the realm name to present to the browser
	 * @throws IOException if a network error occurs
	 */
	protected void challenge(HttpServletResponse rsp, String realm) throws IOException {
		rsp.setHeader("WWW-Authenticate", "Basic realm=" + realm);
		rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
	}
}