// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to support writing Pilot object(s) to the database. This DAO contains helper methods that other
 * DAOs which write to the PILOTS table may access.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class PilotWriteDAO extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	protected PilotWriteDAO(Connection c) {
		super(c);
	}

	/**
	 * Writes a Pilot's security roles to the database.
	 * @param p the Pilot bean
	 * @throws SQLException if a JDBC error occurs
	 */
	protected void writeRoles(Pilot p) throws SQLException {
		prepareStatementWithoutLimits("DELETE FROM ROLES WHERE (ID=?)");
		_ps.setInt(1, p.getID());
		_ps.executeUpdate();
		_ps.close();

		// Write the roles to the database
		prepareStatementWithoutLimits("INSERT INTO ROLES (ID, ROLE) VALUES (?, ?)");
		for (Iterator i = p.getRoles().iterator(); i.hasNext();) {
			String role = (String) i.next();

			// Don't add the "pilot" role
			if (!"Pilot".equals(role)) {
				_ps.setInt(1, p.getID());
				_ps.setString(2, role);
				_ps.addBatch();
			}
		}

		// Execute the batch update
		_ps.executeBatch();
		_ps.close();
	}

	/**
	 * Writes a Pilot's equipment type ratings to the database.
	 * @param p the Pilot bean
	 * @throws SQLException if a JDBC error occurs
	 */
	protected void writeRatings(Pilot p) throws SQLException {
		prepareStatementWithoutLimits("DELETE FROM RATINGS WHERE (ID=?)");
		_ps.setInt(1, p.getID());
		_ps.executeUpdate();
		_ps.close();

		// Write the ratings to the database
		prepareStatementWithoutLimits("INSERT INTO RATINGS (ID, RATING) VALUES (?, ?)");
		for (Iterator i = p.getRatings().iterator(); i.hasNext();) {
			_ps.setInt(1, p.getID());
			_ps.setString(2, (String) i.next());
			_ps.addBatch();
		}

		// Execute the batch update
		_ps.executeBatch();
		_ps.close();
	}
}