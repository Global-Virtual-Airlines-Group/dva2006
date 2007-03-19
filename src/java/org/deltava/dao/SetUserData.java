// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.AirlineInformation;
import org.deltava.beans.system.UserData;

/**
 * A Data Access Object to write cross-applicaton User data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetUserData extends DAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetUserData(Connection c) {
		super(c);
	}

	/**
	 * Writes a User Data entry to the database.
	 * @param usr the UserData object
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(UserData usr) throws DAOException {
		try {
			prepareStatement("REPLACE INTO common.USERDATA (ID, AIRLINE, TABLENAME) VALUES (?, ?, ?)");
			_ps.setInt(1, usr.getID());
			_ps.setString(2, usr.getAirlineCode());
			_ps.setString(3, usr.getTable());

			// Write to the database
			executeUpdate(1);
			if (usr.getID() == 0)
				usr.setID(getNewID());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Removes a User Data entry from the database.
	 * @param id the user's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		GetUserData.invalidate(id);
		try {
			prepareStatement("DELETE FROM common.USERDATA WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Adds Airline Information to the database.
	 * @param info the Airline Information bean
	 * @throws DAOException if a JDBC error occurs
	 * @see SetUserData#update(AirlineInformation)
	 */
	public void write(AirlineInformation info) throws DAOException {
		try {
			prepareStatement("INSERT INFO common.AIRLINEINFO (CODE, NAME, DBNAME, DOMAIN, CAN_TX) VALUES (?, ?, ?, ?, ?)");
			_ps.setString(1, info.getCode());
			_ps.setString(2, info.getName());
			_ps.setString(3, info.getDB());
			_ps.setString(4, info.getDomain());
			_ps.setBoolean(5, info.getCanTransfer());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates Airline Information in the database. <i>This cannot update the airline code or domain name.</i>
	 * @param info the Airline Information bean
	 * @throws DAOException if a JDBC error occurs
	 * @see SetUserData#write(AirlineInformation)
	 */
	public void update(AirlineInformation info) throws DAOException {
		try {
			prepareStatement("UPDATE common.AIRLINEINFO SET NAME=?, DOMAIN=?, CAN_TX=? WHERE (CODE=?)");
			_ps.setString(1, info.getName());
			_ps.setString(2, info.getDomain());
			_ps.setBoolean(3, info.getCanTransfer());
			_ps.setString(4, info.getCode());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}