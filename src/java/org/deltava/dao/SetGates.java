// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.navdata.Gate;
import org.deltava.beans.schedule.Airline;

/**
 * A Data Access Object to write Gate data. 
 * @author Luke
 * @version 6.3
 * @since 6.3
 */

public class SetGates extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetGates(Connection c) {
		super(c);
	}

	/**
	 * Updates airline Gate usage data. 
	 * @param gates a Collection of Gate beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Collection<Gate> gates) throws DAOException {
		try {
			startTransaction();
			
			// Clear gates
			prepareStatementWithoutLimits("DELETE FROM common.GATE_AIRLINES WHERE (ICAO=?) AND (NAME=?)");
			for (Gate g : gates) {
				_ps.setString(1, g.getCode());
				_ps.setString(2, g.getName());
				_ps.addBatch();
			}
			
			_ps.executeBatch();
			_ps.close();
			
			// Write gate data
			prepareStatement("INSERT INTO common.GATE_AIRLINES (ICAO, NAME, AIRLINE, INTL) VALUES (?, ?, ?, ?)");
			for (Gate g : gates) {
				_ps.setString(1, g.getCode());
				_ps.setString(2, g.getName());
				_ps.setBoolean(4, g.isInternational());
				for (Airline a : g.getAirlines()) {
					_ps.setString(3, a.getCode());
					_ps.addBatch();
				}
			}
			
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}