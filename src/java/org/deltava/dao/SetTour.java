// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.ScheduleEntry;
import org.deltava.beans.stats.Tour;

/**
 * A Data Access Object to write Tour data to the database.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class SetTour extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetTour(Connection c) {
		super(c);
	}

	/**
	 * Writes a Tour and its legs to the database.
	 * @param t the Tour
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Tour t) throws DAOException {
		try {
			startTransaction();
			
			// Write the tour
			if (t.getID() == 0) {
				try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO TOURS (NAME, START_DATE, END_DATE, ACTIVE, ACARS_ONLY, ALLOW_OFFLINE, MATCH_EQ, MATCH_LEG) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
					ps.setString(1, t.getName());
					ps.setTimestamp(2, createTimestamp(t.getStartDate()));
					ps.setTimestamp(3, createTimestamp(t.getEndDate()));
					ps.setBoolean(4, t.getActive());
					ps.setBoolean(5, t.getACARSOnly());
					ps.setBoolean(6, t.getAllowOffline());
					ps.setBoolean(7, t.getMatchEquipment());
					ps.setBoolean(8, t.getMatchLeg());
					executeUpdate(ps, 1);
				}
			
				t.setID(getNewID());
			} else {
				try (PreparedStatement ps = prepareWithoutLimits("UPDATE TOURS SET NAME=?, START_DATE=?, END_DATE=?, ACTIVE=?, ACARS_ONLY=?, ALLOW_OFFLINE=?, MATCH_EQ=?, MATCH_LEG=? WHERE (ID=?)")) {
					ps.setString(1, t.getName());
					ps.setTimestamp(2, createTimestamp(t.getStartDate()));
					ps.setTimestamp(3, createTimestamp(t.getEndDate()));
					ps.setBoolean(4, t.getActive());
					ps.setBoolean(5, t.getACARSOnly());
					ps.setBoolean(6, t.getAllowOffline());
					ps.setBoolean(7, t.getMatchEquipment());
					ps.setBoolean(8, t.getMatchLeg());
					ps.setInt(9, t.getID());
					executeUpdate(ps, 1);
				}
				
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM TOUR_NETWORKS WHERE (ID=?)")) {
					ps.setInt(1, t.getID());
					executeUpdate(ps, 0);
				}
			
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM TOUR_LEGS WHERE (ID=?)")) {
					ps.setInt(1, t.getID());
					executeUpdate(ps, 0);
				}
			}
			
			// Write networks
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO TOUR_NETWORKS (ID, NETWORK) VALUES (?, ?)")) {
				ps.setInt(1, t.getID());
				for (OnlineNetwork net : t.getNetworks()) {
					ps.setInt(2, net.ordinal());
					ps.addBatch();
				}
				
				executeUpdate(ps, 1, t.getNetworks().size());
			}
			
			// Write briefing
			if (t.isLoaded()) {
				try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO TOUR_BRIEFINGS (ID, ISPDF, SIZE, DATA) VALUES (?, ?, ?, ?)")) {
					ps.setInt(1, t.getID());
					ps.setBoolean(2, t.getIsPDF());
					ps.setInt(3, t.getSize());
					ps.setBinaryStream(4, t.getInputStream());
					executeUpdate(ps, 1);
				}
			} else {
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM TOUR_BRIEFINGS WHERE (ID=?)")) {
					ps.setInt(1, t.getID());
					executeUpdate(ps, 0);
				}
			}
			
			// Write the legs
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO TOUR_LEGS (ID, IDX, AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, EQTYPE, TIME_D, TIME_A, FLIGHT_TIME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				int idx = 0;
				ps.setInt(1, t.getID());
				for (ScheduleEntry se : t.getFlights()) {
					ps.setInt(2, ++idx);
					ps.setString(3, se.getAirline().getCode());
					ps.setInt(4, se.getFlightNumber());
					ps.setInt(5, se.getLeg());
					ps.setString(6, se.getAirportD().getIATA());
					ps.setString(7, se.getAirportA().getIATA());
					ps.setString(8, se.getEquipmentType());
					ps.setTimestamp(9, Timestamp.valueOf(se.getTimeD().toLocalDateTime()));
					ps.setTimestamp(10, Timestamp.valueOf(se.getTimeA().toLocalDateTime()));
					ps.setInt(11, se.getLength());
					ps.addBatch();
				}
				
				executeUpdate(ps, 1, idx);
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Tour from the database, clearing the flag from any existing Flight Reports.
	 * @param t the Tour
	 * @return the number of Flight Reports updated
	 * @throws DAOException if a JDBC error occurs
	 */
	public int delete(Tour t) throws DAOException {
		try {
			int legs = 0; startTransaction(); 
			
			// Delete the Tour
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM TOURS WHERE (ID=?)")) {
				ps.setInt(1, t.getID());
				executeUpdate(ps, 0);
			}
			
			// Clear the Flight reports
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE PIREPS SET TOUR_ID=0 WHERE (TOUR_ID=?)")) {
				ps.setInt(1, t.getID());
				legs = executeUpdate(ps, 0);
			}
			
			commitTransaction();
			return legs;
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}		
	}
}