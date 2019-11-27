// Copyright 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.*;
import org.deltava.beans.navdata.RunwayMapping;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read runway renumbering data. 
 * @author Luke
 * @version 9.0
 * @since 8.3
 */

public class GetRunwayMapping extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetRunwayMapping(Connection c) {
		super(c);
	}
	
	/**
	 * Checks for a runway mapping. 
	 * @param a the ICAOAirport
	 * @param oldCode the old runway code
	 * @return a RunwayMapping bean, or null if not renumbered
	 * @throws DAOException if a JDBC error occurs
	 */
	public RunwayMapping get(ICAOAirport a, String oldCode) throws DAOException {
		Collection<RunwayMapping> maps = getAll(a);
		return maps.stream().filter(rm -> (rm.getOldCode().equalsIgnoreCase(oldCode))).findAny().orElse(null);
	}

	/**
	 * Returns all runway mappings for a particular Airport.
	 * @param a an ICAOAirport
	 * @return a Collection of RunwayMapping beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<RunwayMapping> getAll(ICAOAirport a) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT OLDCODE, NEWCODE FROM common.RUNWAY_RENUMBER WHERE (ICAO=?)")) {
			ps.setString(1,  a.getICAO());
			
			Collection<RunwayMapping> results = new ArrayList<RunwayMapping>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					RunwayMapping rm = new RunwayMapping(a.getICAO());
					rm.setOldCode(rs.getString(1));
					rm.setNewCode(rs.getString(2));
					results.add(rm);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Airports with a remapped runway.
	 * @return a Collection of Airports
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Airport> getAirports() throws DAOException {
		try (PreparedStatement ps = prepare("SELECT DISTINCT ICAO FROM common.RUNWAY_RENUMBER")) {
			Collection<Airport> results = new ArrayList<Airport>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Airport a = SystemData.getAirport(rs.getString(1));
					if (a != null)
						results.add(a);
				}
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}