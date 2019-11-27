// Copyright 2005, 2007, 2008, 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.SelectCall;

/**
 * A Data Access Object to read aircraft SELCAL data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetSELCAL extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetSELCAL(Connection c) {
		super(c);
	}
	
	/**
	 * Returns SELCAL data for a particular code.
	 * @param code the SELCAL code
	 * @return a SelectCall or null if not found
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if code is null
	 */
	public SelectCall get(String code) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("SELECT * FROM SELCAL WHERE (CODE=?) LIMIT 1")) {
			ps.setString(1, code.toUpperCase());
			return execute(ps).stream().findFirst().orElse(null);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all aircraft types that have a SELCAL code.
	 * @return a Collection of aircraft types
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getEquipmentTypes() throws DAOException {
		Collection<String> results = new LinkedHashSet<String>();
		try (PreparedStatement ps = prepareWithoutLimits("SELECT DISTINCT EQTYPE FROM SELCAL ORDER BY EQTYPE")) {
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
	 * Returns all aircraft SELCAL codes.
	 * @param orderBy the column to sort results with
	 * @return a Collection of SelectCall beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<SelectCall> getCodes(String orderBy) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT SC.*, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME) AS PNAME FROM SELCAL SC LEFT JOIN PILOTS P ON (P.ID=SC.PILOT_ID) ORDER BY " + orderBy)) {
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all reserved aircraft SELCAL codes.
	 * @param pilotID the Pilot's database ID
	 * @return a Collection of SelectCall beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<SelectCall> getReserved(int pilotID) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM SELCAL WHERE (PILOT_ID=?) ORDER BY CODE")) {
			ps.setInt(1, pilotID);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all aircraft SELCAL codes for a particular aircraft type.
	 * @param eqType the equipment type
	 * @return a Collection of SelectCall beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<SelectCall> getByEquipmentType(String eqType) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT * FROM SELCAL WHERE (EQTYPE=?) ORDER BY CODE")) {
			ps.setString(1, eqType);
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse result sets.
	 */
	private static List<SelectCall> execute(PreparedStatement ps) throws SQLException {
		List<SelectCall> results = new ArrayList<SelectCall>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				SelectCall sc = new SelectCall(rs.getString(1), rs.getString(2));
				sc.setEquipmentType(rs.getString(3));
				sc.setReservedBy(rs.getInt(4));
				sc.setReservedOn(toInstant(rs.getTimestamp(5)));
				results.add(sc);
			}
		}
		
		return results;
	}
}