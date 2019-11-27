// Copyright 2014, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Airport;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load ACARS Track information.
 * @author Luke
 * @version 9.0
 * @since 5.4
 */

public class GetACARSTrackInfo extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetACARSTrackInfo(Connection c) {
		super(c);
	}

	/**
	 * Loads local Airports shown on the ACARS track map.
	 * @return a Collection of Airport beans, sorted by popularity
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getLocalAirports() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT IATA FROM acars.TRACK_AIRPORTS ORDER BY CNT DESC")) {
			Collection<Airport> results = new LinkedHashSet<Airport>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(SystemData.getAirport(rs.getString(1)));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}