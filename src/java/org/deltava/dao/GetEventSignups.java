// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to load Online Event signup totals.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class GetEventSignups extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetEventSignups(Connection c) {
		super(c);
	}

	/**
	 * Loads Online Event signup totals for a group of Pilots.
	 * @param pilots a Map of Pilot objects to populate with results
	 * @throws DAOException if a JDBC error occurs
	 */
	public void getSignupTotals(Map<Integer, Pilot> pilots) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT ES.PILOT_ID, COUNT(ES.ID) FROM events.EVENT_SIGNUPS ES "
				+ "WHERE ES.PILOT_ID IN (");
		
		// Append the Pilot IDs
		int setSize = 0;
		for (Iterator<Pilot> i = pilots.values().iterator(); i.hasNext();) {
			Pilot p = i.next();
			if (p.getEventSignups() == -1) {
				setSize++;
				sqlBuf.append(p.getID());
				sqlBuf.append(',');
			}
		}
		
		// Strip out trailing comma
		if (sqlBuf.charAt(sqlBuf.length() - 1) == ',')
			sqlBuf.setLength(sqlBuf.length() - 1);
		
		// Close the SQL statement
		sqlBuf.append(") GROUP BY ES.PILOT_ID LIMIT ");
		sqlBuf.append(String.valueOf(setSize));
		if (setSize == 0)
			return;
		
		try {
			prepareStatementWithoutLimits(sqlBuf.toString());
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next()) {
				Pilot p = pilots.get(new Integer(rs.getInt(1)));
				if (p != null)
					p.setEventSignups(rs.getInt(2));
			}
			
			// Clean up
			rs.close();
			_ps.close();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}