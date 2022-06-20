// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.List;

import org.deltava.beans.schedule.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to retrieve ACARS altenrate airport statistics.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class GetACARSAlternate extends DAO {
	
	private static final Cache<CacheableList<Airport>> _cache = CacheManager.getCollection(Airport.class, "Alternates");
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSAlternate(Connection c) {
		super(c);
	}

	/**
	 * Retrieves popular alternates for a particular Airport.
	 * @param a the Airport
	 * @return a list of alternate Airports, sorted by descending popularity
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<Airport> getAlternates(Airport a) throws DAOException {
		
		// Check the cache
		CacheableList<Airport> results = _cache.get(a.getIATA());
		if (results != null)
			return results.clone();
		
		try (PreparedStatement ps = prepare("SELECT F.AIRPORT_L, COUNT(F.ID) AS CNT FROM acars.FLIGHTS F WHERE (F.AIRPORT_A=?) AND (F.AIRPORT_L IS NOT NULL) GROUP BY F.AIRPORT_L ORDER BY CNT DESC")) {
			ps.setString(1, a.getIATA());
			results = new CacheableList<Airport>(a.getIATA());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(SystemData.getAirport(rs.getString(1)));
			}
			
			_cache.add(results);
			return results.clone();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}