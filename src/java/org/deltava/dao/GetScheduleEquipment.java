// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.RoutePair;

/**
 * A Data Access Object to load equipment types from the Flight Schedule.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class GetScheduleEquipment extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetScheduleEquipment(Connection c) {
		super(c);
	}

	/**
	 * Returns equipment used on a particular route pair.
	 * @param rp the RotuePair
	 * @param a the Airline, or null for all
	 * @return a Collection of equipment type names
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getEquipmentTypes(RoutePair rp, Airline a) throws DAOException {
		
		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("SELECT DISTINCT EQTYPE FROM SCHEDULE WHERE (AIRPORT_D=?) AND (AIRPORT_A=?)");
		if (a != null)
			sqlBuf.append(" AND (AIRLINE=?)");
		sqlBuf.append("ORDER BY RAND()");
		
		try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
			ps.setString(1, rp.getAirportD().getIATA());
			ps.setString(2, rp.getAirportA().getIATA());
			if (a != null)
				ps.setString(3, a.getCode());
			
			Collection<String> results = new LinkedHashSet<String>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns all equipment types used by a particular Airline.
	 * @param a an Airline
	 * @return a Collection of equipment type names
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getEquipmentTypes(Airline a) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT EQTYPE FROM SCHEDULE WHERE (AIRLINE=?)")) {
			ps.setString(1,  a.getCode());
			
			Collection<String> results = new LinkedHashSet<String>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}