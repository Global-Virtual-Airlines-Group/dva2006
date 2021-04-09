// Copyright 2005, 2006, 2007, 2009, 2010, 2015, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.lifecycle;

import java.sql.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.security.UserPool;

import org.deltava.dao.SetPilotLogin;

import org.deltava.commands.*;

import org.deltava.util.system.SystemData;
import org.gvagroup.jdbc.ConnectionPool;
import org.gvagroup.jdbc.ConnectionPoolException;

/**
 * A servlet lifecycle event listener to handle user logins and logouts.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class UserListener implements HttpSessionListener {

	private static final Logger log = Logger.getLogger(UserListener.class);

	/**
	 * Called on the creation of a new HTTP session.
	 * @param e the lifecycle event
	 */
	@Override
	public void sessionCreated(HttpSessionEvent e) {
		HttpSession s = e.getSession();
		s.setAttribute(CommandContext.USRLISTENER_ATTR_NAME, UserStartupListener.INSTANCE);
		if (log.isDebugEnabled())
			log.debug("Created Session " + s.getId());
	}
	
	/**
	 * Called on the termination of an HTTP session.
	 * @param e the lifecycle event
	 */
	@Override
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
			con = jdbcPool.getConnection();
			SetPilotLogin pldao = new SetPilotLogin(con);
			pldao.logout(p.getID(), SystemData.get("airline.db"));
		} catch (ConnectionPoolException cpe) {
			log.warn(cpe.getMessage());
		} catch (Exception ex) {
			log.error("Error logging session close for " + p.getID() + " - " + ex.getMessage(), ex);
		} finally {
			jdbcPool.release(con);
		}
	}
}