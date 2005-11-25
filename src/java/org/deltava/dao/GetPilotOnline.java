// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.Pilot;

/**
 * A Data Access Object to load Pilot data for Online Network operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetPilotOnline extends PilotReadDAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPilotOnline(Connection c) {
		super(c);
	}

	/**
	 * Returns the network IDs for all Active/On leave pilots. This will return a Map with the network ID as the key, and
	 * the <i>database ID</i> of the Pilot as the value, allowing easy lookups of Pilots based on network ID.
	 * @param network the network name (VATSIM or IVAO)
	 * @return a Map of network ID/database ID pairs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, Integer> getIDs(String network) throws DAOException {
		
		try {
			// Prepare the statement
			String colName = network.toUpperCase() + "_ID";
			prepareStatement("SELECT ID, " + colName + " FROM PILOTS WHERE (STATUS IN (?, ?)) AND (" +
					colName + " IS NOT NULL)");
			_ps.setInt(1, Pilot.ACTIVE);
			_ps.setInt(2, Pilot.ON_LEAVE);
			
			// Execute the Query
			Map<String, Integer> results = new HashMap<String, Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.put(rs.getString(2), new Integer(rs.getInt(1)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}