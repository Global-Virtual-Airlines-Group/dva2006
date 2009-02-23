// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.lifecycle;

import java.sql.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.security.UserPool;

import org.deltava.dao.SetPilotLogin;

import org.deltava.commands.*;
import org.deltava.jdbc.*;

import org.deltava.util.system.SystemData;

/**
 * A servlet lifecycle event listener to handle user logins and logouts.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class UserListener implements HttpSessionListener {

	private static final Logger log = Logger.getLogger(UserListener.class);

	/**
	 * Called on the creation of a new HTTP session.
	 * @param e the lifecycle event
	 */
	public void sessionCreated(HttpSessionEvent e) {
		HttpSession s = e.getSession();
		s.setAttribute(CommandContext.USRLISTENER_ATTR_NAME, new UserStartupListener());
		if (log.isDebugEnabled())
			log.debug("Created Session " + s.getId());
	}
	
	/**
	 * Called on the termination of an HTTP session.
	 * @param e the lifecycle event
	 */
	public void sessionDestroyed(HttpSessionEvent e) {
		HttpSession s = e.getSession();
		if (log.isDebugEnabled())
			log.debug("Destroyed Session " + s.getId());

		// Get the user object
		Person p = (Person) s.getAttribute(HTTPContext.USER_ATTR_NAME);
		if (p == null)
			return;

		// Log the user off
		UserPool.remove(p, s.getId());
		log.info(p.getName() + " logged out");

		// Get the JDBC connection pool and a system connection
		ConnectionPool jdbcPool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
		Connection con = null;
		try {
			// Update the user's last login date
			con = jdbcPool.getConnection(true);
			SetPilotLogin pldao = new SetPilotLogin(con);
			pldao.logout(p.getID());
		} catch (ConnectionPoolException cpe) {
			log.warn(cpe.getMessage(), cpe.getLogStackDump() ? cpe : null);
		} catch (Exception ex) {
			log.error("Error logging session close - " + ex.getMessage(), ex);
		} finally {
			jdbcPool.release(con);
		}
	}
}