// Copyright 2005, 2006, 2008, 2010, 2011, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.cache.*;

/**
 * A Data Access Object to load Pilot data for Online Network operations.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class GetPilotOnline extends PilotReadDAO {
	
	private static final Cache<CacheableMap<String, Integer>> _idCache = CacheManager.getMap(String.class, Integer.class, "OnlineIDs");
	
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
	 * @param network the network
	 * @return a Map of network ID/database ID pairs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<String, Integer> getIDs(OnlineNetwork network) throws DAOException {
		
		// This only supports VATSIM/IVAO/PE
		if ((network != OnlineNetwork.VATSIM) && (network != OnlineNetwork.IVAO) && (network != OnlineNetwork.PILOTEDGE))
			return Collections.emptyMap();
		
		// Check the cache
		CacheableMap<String, Integer> results = _idCache.get(network);
		if (results != null)
			return new LinkedHashMap<String, Integer>(results); 
		
		try {
			// Prepare the statement
			String colName = (network == OnlineNetwork.PILOTEDGE ? "PE" : network.toString()) + "_ID";
			prepareStatement("SELECT ID, " + colName + " FROM PILOTS WHERE ((STATUS=?) OR (STATUS=?)) AND (" + colName + " IS NOT NULL)");
			_ps.setInt(1, Pilot.ACTIVE);
			_ps.setInt(2, Pilot.ON_LEAVE);
			
			// Execute the Query
			results = new CacheableMap<String, Integer>(network);
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.put(rs.getString(2), Integer.valueOf(rs.getInt(1)));
			}
			
			_ps.close();
			_idCache.add(results);
			return new LinkedHashMap<String, Integer>(results); 
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Pilots registered with an ID in a particular online network. <i>Flight Totals will not be populated</i>.
	 * @param network the online network
	 * @return a List of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Pilot> getPilots(OnlineNetwork network) throws DAOException {
		if ((network != OnlineNetwork.VATSIM) && (network != OnlineNetwork.IVAO) && (network != OnlineNetwork.PILOTEDGE))
			return Collections.emptyList();
		
		// Build the SQL statement
		String colName = (network == OnlineNetwork.PILOTEDGE ? "PE" : network.toString()) + "_ID";
		StringBuilder sqlBuf = new StringBuilder("SELECT P.ID FROM PILOTS P WHERE ((P.STATUS=?) OR (P.STATUS=?)) AND (LENGTH(");
		sqlBuf.append(colName);
		sqlBuf.append(") > 0)");
		
		try {
			prepareStatement(sqlBuf.toString());
			_ps.setInt(1, Pilot.ACTIVE);
			_ps.setInt(2, Pilot.ON_LEAVE);
			return getByID(executeIDs(), "PILOTS").values();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}