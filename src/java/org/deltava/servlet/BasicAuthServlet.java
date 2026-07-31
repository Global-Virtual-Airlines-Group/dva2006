// Copyright 2005, 2007, 2010, 2012, 2014, 2015, 2017, 2018, 2020, 2023, 2024, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;
import java.io.IOException;

import jakarta.servlet.http.*;

import org.apache.logging.log4j.*;
import org.deltava.beans.Pilot;
import org.deltava.dao.*;
import org.deltava.security.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.pool.*;

/**
 * A servlet that supports basic HTTP authentication.
 * @author Luke
 * @version 12.5
 * @since 1.0
 */

abstract class BasicAuthServlet extends GenericServlet {

	private static final Logger log = LogManager.getLogger(BasicAuthServlet.class);
	
	/**
	 * Dummy system user class.
	 */
	static class SystemUser extends Pilot {
		public SystemUser() {
			super("Golgotha", "SYSTEM");
			setLastLogin(Instant.now());
		}
		
		@Override
		public boolean isInRole(String roleName) {
			return true;
		}
	}

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
		
		// Check for Golgotha
		String userID = tkns.nextToken(); String pwd = tkns.nextToken();
		if ("Golgotha".equals(userID)) {
			String sysPwd = SystemData.get("security.key.golgotha");
			if (pwd.equals(sysPwd))
				return new SystemUser();
		}

		// Get the JDBC Connection Pool
		ConnectionPool<Connection> pool = SystemData.getJDBCPool();

		Connection con = null;
		Pilot p = null;
		try {
			con = pool.getConnection();

			// Get the DAO and the directory name for this user
			GetPilotDirectory dao = new GetPilotDirectory(con);
			UserID id = new UserID(userID); 
			Pilot usr = id.hasAirlineCode() ? dao.getByCode(userID) : dao.get(id.getUserID());
			if (usr == null)
				throw new SecurityException(String.format("Unknown User ID - %s", userID));
			
			// Authenticate the user
			try (Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR)) {
				if (auth instanceof SQLAuthenticator sa) sa.setConnection(con);
				auth.authenticate(usr, pwd);
			}
			
			p = usr;
		} catch (SecurityException se) {
			log.warn("Authentication failure - {}", se.getMessage());
		} catch (DAOException de) {
			log.warn("Data load failure - {}", de.getMessage());
		} catch (ConnectionPoolException cpe) {
			log.warn("Connection pool error - {}", cpe.getMessage());
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
		rsp.setHeader("WWW-Authenticate", String.format("Basic realm=%s", realm));
		rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
	}
}