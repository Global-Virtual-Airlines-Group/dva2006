// Copyright 2005, 2006, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.Staff;

/**
 * A Data Access Object to write Staff Profiles to the database.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class SetStaff extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetStaff(Connection c) {
		super(c);
	}

	/**
	 * Writes/Updates a Staff Profile to the database.
	 * @param s the Staff Profile
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Staff s) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO STAFF (ID, TITLE, SORT_ORDER, BIO, AREA) VALUES (?, ?, ?, ?, ?)")) {
			ps.setInt(1, s.getID());
			ps.setString(2, s.getTitle());
			ps.setInt(3, s.getSortOrder());
			ps.setString(4, s.getBody());
			ps.setString(5, s.getArea());
			
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Removes a Staff Profile from the databse.
	 * @param id the Staff Profile ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM STAFF WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}