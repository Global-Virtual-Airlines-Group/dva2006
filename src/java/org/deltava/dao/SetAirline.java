// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2017, 2019, 2020, 2021, 2024, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to update Airline information.
 * @author Luke
 * @version 12.5
 * @since 8.0
 */

public class SetAirline extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAirline(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Airline to the Schedule.
	 * @param al the Airline bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(Airline al) throws DAOException {
		try {
			startTransaction();
			
			// Write the airline data
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRLINES (CODE, NAME, ICAO, COLOR, MIN_CS, ACTIVE, SYNC, HISTORIC) VALUES (?,?,?,?,?,?,?,?)")) {
				ps.setString(1, al.getCode());
				ps.setString(2, al.getName());
				ps.setString(3, al.getICAO());
				ps.setString(4, al.getColor());
				ps.setInt(5, al.getMinimumCodeShare());
				ps.setBoolean(6, al.getActive());
				ps.setBoolean(7, al.getScheduleSync());
				ps.setBoolean(8, al.getHistoric());
				executeUpdate(ps, 1);
			}
			
			// Write the alternate codes
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRLINE_CODES (CODE, ALTCODE) VALUES (?,?)")) {
				ps.setString(1, al.getCode());
				for (String code : al.getCodes()) {
					if (!code.equals(al.getCode())) {
						ps.setString(2, code);
						ps.addBatch();
					}
				}
			
				executeUpdate(ps, 1, 0);
			}
			
			// Write airline link coddes
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRLINE_LINKS (CODE, ASSOC) VALUES (?,?)")) {
				ps.setString(1, al.getCode());
				for (String code : al.getAssociatedAirlines()) {
					ps.setString(2, code);
					ps.addBatch();
				}
				
				executeUpdate(ps, 1, al.getAssociatedAirlines().size());
			}
			
			// Write the webapp data
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.APP_AIRLINES (CODE, APPCODE) VALUES (?,?)")) {
				ps.setString(1, al.getCode());
				for (Iterator<String> i = al.getApplications().iterator(); i.hasNext(); ) {
					ps.setString(2, i.next());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, al.getApplications().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Airline in the Schedule.
	 * @param al the Airline bean
	 * @param oldCode the old airline code
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Airline al, String oldCode) throws DAOException {
		try {
			startTransaction();
			
			// Clear the alternate code data
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.AIRLINE_CODES WHERE (CODE=?)")) {
				ps.setString(1, oldCode);
				executeUpdate(ps, 0);
			}
			
			// Clear the webapp data
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.APP_AIRLINES WHERE (CODE=?)")) {
				ps.setString(1, oldCode);
				executeUpdate(ps, 0);
			}
			
			// Clear linked codes
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.AIRLINE_LINKS WHERE (CODE=?)")) {
				ps.setString(1, oldCode);
				executeUpdate(ps, 0);
			}
			
			// Write the airline data
			try (PreparedStatement ps = prepare("UPDATE common.AIRLINES SET NAME=?, ICAO=?, COLOR=?, ACTIVE=?, CODE=?, SYNC=?, HISTORIC=?, MIN_CS=? WHERE (CODE=?)")) {
				ps.setString(1, al.getName());
				ps.setString(2, al.getICAO());
				ps.setString(3, al.getColor());
				ps.setBoolean(4, al.getActive());
				ps.setString(5, al.getCode());
				ps.setBoolean(6, al.getScheduleSync());
				ps.setBoolean(7, al.getHistoric());
				ps.setInt(8, al.getMinimumCodeShare());
				ps.setString(9, oldCode);
				executeUpdate(ps, 1);
			}
			
			// Write the alternate codes
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRLINE_CODES (CODE, ALTCODE) VALUES (?,?)")) {
				ps.setString(1, al.getCode());
				for (String code : al.getCodes()) {
					if (!code.equals(al.getCode())) {
						ps.setString(2, code);
						ps.addBatch();
					}
				}
			
				executeUpdate(ps, 1, 0);
			}
			
			// Write airline link coddes
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRLINE_LINKS (CODE, ASSOC) VALUES (?,?)")) {
				ps.setString(1, al.getCode());
				for (String code : al.getAssociatedAirlines()) {
					ps.setString(2, code);
					ps.addBatch();
				}
					
				executeUpdate(ps, 1, al.getAssociatedAirlines().size());
			}
			
			// Write the webapp data
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.APP_AIRLINES (CODE, APPCODE) VALUES (?,?)")) {
				ps.setString(1, al.getCode());
				for (String code : al.getApplications()) {
					ps.setString(2, code);
					ps.addBatch();
				}

				executeUpdate(ps, 1, al.getApplications().size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an Airline from the Schedule. This operation may fail if there are any Flight Reports that reference
	 * this Airline. In such a case, it is best to disable the Airline.
	 * @param a the Airline bean
	 * @throws DAOException if a JDBC error occurs
	 * @throws NullPointerException if a is null
	 */
	public void delete(Airline a) throws DAOException {
		try (PreparedStatement ps = prepare("DELETE FROM common.AIRLINES WHERE (CODE=?)")) {
			ps.setString(1, a.getCode());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}