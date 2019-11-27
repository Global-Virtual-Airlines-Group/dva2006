// Copyright 2009, 2011, 2012, 2014, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.FuzzyPosition;

/**
 * A Data Access Object to display pilot locations.
 * @author Luke
 * @version 9.0
 * @since 2.5
 */

public class GetPilotBoard extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPilotBoard(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the location of the specified Pilot.
	 * @param pilotID the Pilot's database ID
	 * @return a GeoLocation with the pilot's location, or null if none specified
	 * @throws DAOException
	 */
	public GeoLocation getLocation(int pilotID) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT LAT, LNG, H FROM PILOT_MAP WHERE (ID=?) LIMIT 1")) {
			ps.setInt(1, pilotID);
			GeoLocation gl = null;
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					gl = new FuzzyPosition(new GeoPosition(rs.getDouble(1), rs.getDouble(2)), rs.getFloat(3));
			}
			
			return gl;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the locations of all Pilots who have signed up for the Pilot location board.
	 * @return a Map of GeoLocation objects, keyed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, GeoLocation> getAll() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM PILOT_MAP ORDER BY ID")) {
			Map<Integer, GeoLocation> results = new LinkedHashMap<Integer, GeoLocation>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					GeoPosition gp = new GeoPosition(rs.getDouble(2), rs.getDouble(3));
					results.put(Integer.valueOf(rs.getInt(1)), new FuzzyPosition(gp, rs.getFloat(4)));
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the locations of active/on leave Pilots who have signed up for the Pilot location board.
	 * @return a Map of GeoLocation objects, keyed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, GeoLocation> getActive() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT M.* FROM PILOT_MAP M, PILOTS P WHERE (M.ID=P.ID) AND ((P.STATUS=?) OR (P.STATUS=?)) ORDER BY M.ID")) {
			ps.setInt(1, Pilot.ACTIVE);
			ps.setInt(2, Pilot.ON_LEAVE);

			// Execute the query
			Map<Integer, GeoLocation> results = new LinkedHashMap<Integer, GeoLocation>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					GeoPosition gp = new GeoPosition(rs.getDouble(2), rs.getDouble(3));
					results.put(Integer.valueOf(rs.getInt(1)), new FuzzyPosition(gp, rs.getFloat(4)));
				}
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the location of Pilots who have signed up for the Pilot location board.
	 * @param ids a Collection of Pilot database IDs
	 * @return a Map of GeoLocation objects, keyed by database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public Map<Integer, GeoLocation> getByID(Collection<Integer> ids) throws DAOException {
		if (ids.isEmpty()) return Collections.emptyMap();
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT * FROM PILOT_MAP WHERE ID IN (");
		for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
			Integer id = i.next();
			buf.append(id.toString());
			if (i.hasNext())
				buf.append(',');
		}
		
		buf.append(") ORDER BY ID");
		
		try (PreparedStatement ps = prepareWithoutLimits(buf.toString())) {
			Map<Integer, GeoLocation> results = new LinkedHashMap<Integer, GeoLocation>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					GeoPosition gp = new GeoPosition(rs.getDouble(2), rs.getDouble(3));
					results.put(Integer.valueOf(rs.getInt(1)), new FuzzyPosition(gp, rs.getFloat(4)));
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}