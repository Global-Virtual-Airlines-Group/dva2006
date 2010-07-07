// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to load ACARS Dispatch statistics.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class GetDispatchStatistics extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetDispatchStatistics(Connection c) {
		super(c);
	}

	/**
	 * Updates ACARS Dispatch totals for a single Pilot.
	 * @param p the Pilot
	 * @param dbName the database
	 * @throws DAOException if a JDBC error occurs
	 */
	public void getDispatchTotals(Pilot p, String dbName) throws DAOException {
		Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>(2);
		pilots.put(new Integer(p.getID()), p);
		getDispatchTotals(pilots, dbName);
	}
	
	/**
	 * Updates ACARS Dispatch totals for a group of Pilots.
	 * @param pilots a Map of Pilots, keyed by database ID
	 * @param dbName the database
	 * @throws DAOException if a JDBC error occurs
	 */
	public void getDispatchTotals(Map<Integer, Pilot> pilots, String dbName) throws DAOException {
		
		// TODO Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT FR.PILOT_ID, COUNT(FR.ID), ");
		
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}