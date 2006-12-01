// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Aircraft;
import org.deltava.beans.system.AirlineInformation; 

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Aircraft data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetAircraft extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAircraft(Connection c) {
		super(c);
	}
	
	/**
	 * Loads a particular aircraft profile.
	 * @param name the aircraft name
	 * @return the Aircraft bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Aircraft get(String name) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM common.AIRCRAFT WHERE (NAME=?)");
			_ps.setString(1, name);
			
			// Load the result
			List<Aircraft> results = execute();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all aircraft profiles.
	 * @return a Collection of Aircraft beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Aircraft> getAll() throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.AIRCRAFT");
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all aircraft used by a particular airline.
	 * @param al the webapp profile bean
	 * @return a Collection of Aircraft beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Aircraft> getAircraftTypes(AirlineInformation al) throws DAOException {
		try {
			prepareStatement("SELECT * FROM common.AIRCRAFT A, common.AIRCRAFT_AIRLINE AA WHERE "
					+ "(A.NAME=AA.NAME) AND (AA.AIRLINE=?)");
			_ps.setString(1, al.getCode());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Helper method to process result sets.
	 */
	private List<Aircraft> execute() throws SQLException {
		Map<String, Aircraft> results = new LinkedHashMap<String, Aircraft>();
		
		// Exeucte the query
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			Aircraft a = new Aircraft(rs.getString(1));
			a.setRange(rs.getInt(2));
			a.setIATA(StringUtils.split(rs.getString(3), ","));
			results.put(a.getName(), a);
		}
		
		// Clean up
		rs.close();
		_ps.close();
		
		// Load the webapp data
		prepareStatementWithoutLimits("SELECT * FROM common.AIRCRAFT_AIRLINE");
		rs = _ps.executeQuery();
		while (rs.next()) {
			String alCode = rs.getString(2);
			Aircraft a = results.get(rs.getString(1));
			AirlineInformation al = SystemData.getApp(alCode);
			if ((al != null) && (a != null))
				a.addApp(al);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return new ArrayList<Aircraft>(results.values());
	}
}