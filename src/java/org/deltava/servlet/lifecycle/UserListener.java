package org.deltava.servlet.lifecycle;

import java.sql.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.Person;
import org.deltava.security.UserPool;

import org.deltava.dao.SetPilotLogin;
import org.deltava.dao.SetSystemData;
import org.deltava.dao.DAOException;

import org.deltava.commands.CommandContext;
import org.deltava.jdbc.ConnectionPool;

import org.deltava.util.system.SystemData;

/**
 * A servlet lifecycle event listener to handle user logins and logouts.
 * @author Luke
 * @version 1.0
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
		log.debug("Created Session " + s.getId());

		// Get the JDBC connection pool and a system connection
		ConnectionPool jdbcPool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
		Connection con = null;

		// Log the session creation
		try {
			con = jdbcPool.getSystemConnection();
			SetSystemData wdao = new SetSystemData(con);
			wdao.openSession(s);
		} catch (DAOException de) {
			log.error("Error logging session create - " + de.getMessage(), de);
		} finally {
			jdbcPool.release(con);
		}
	}

	/**
	 * Called on the termination of an HTTP session.
	 * @param e the lifecycle event
	 */
	public void sessionDestroyed(HttpSessionEvent e) {
		HttpSession s = e.getSession();

		// Log session destruction
		log.debug("Destroyed Session " + s.getId());

		// Get the user object
		Person p = (Person) s.getAttribute(CommandContext.USER_ATTR_NAME);

		// Log the user off
		if (p != null) {
		   p.logoff();
		   UserPool.removePerson(p, s.getId());
		   log.info(p.getName() + " logged out");
		}

		// Get the JDBC connection pool and a system connection
		ConnectionPool jdbcPool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
		Connection con = null;
		try {
			con = jdbcPool.getSystemConnection();
			
			// Log the session close, or delete the session if for anonymous
			SetSystemData swdao = new SetSystemData(con);
			if (p == null) {
			   swdao.deleteSession(s.getId());
			} else {
			   swdao.closeSession(s.getId());
			   
				// Update the user's last login date
				SetPilotLogin pldao = new SetPilotLogin(con);
				pldao.logout((Pilot) p);
			}
		} catch (Exception ex) {
			log.error("Error logging session close - " + ex.getMessage(), ex);
		} finally {
			jdbcPool.release(con);
		}
	}
}