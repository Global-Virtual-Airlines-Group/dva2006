// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.schedule.*;

/**
 * A Data Access Object to update the Flight Schedule.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetSchedule extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetSchedule(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Airline to the Schedule.
	 * @param al the Airline bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(Airline al) throws DAOException {
		try {
			prepareStatement("INSERT INTO common.AIRLINES (CODE, NAME, ACTIVE) VALUES (?, ?, ?)");
			_ps.setString(1, al.getCode());
			_ps.setString(2, al.getName());
			_ps.setBoolean(3, al.getActive());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Airline in the Schedule.
	 * @param al the Airline bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Airline al) throws DAOException {
		try {
			prepareStatement("UPDATE common.AIRLINES SET NAME=?, ACTIVE=? WHERE (CODE=?)");
			_ps.setString(1, al.getName());
			_ps.setBoolean(2, al.getActive());
			_ps.setString(3, al.getCode());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes a new Airport to the Schedule.
	 * @param a the Airport bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(Airport a) throws DAOException {
		try {
			startTransaction();
			
			// Write the airport data
			prepareStatement("INSERT INTO common.AIRPORTS (IATA, ICAO, TZ, NAME, LATITUDE, LONGITUDE) VALUES (?, ?, ?, ?, ?, ?)");
			_ps.setString(1, a.getIATA());
			_ps.setString(2, a.getICAO());
			_ps.setString(3, a.getTZ().getID());
			_ps.setString(4, a.getName());
			_ps.setDouble(5, a.getLatitude());
			_ps.setDouble(6, a.getLongitude());
			executeUpdate(1);
			
			// Write the airline data
			prepareStatement("INSERT INTO common.AIRPORT_AIRLINE (CODE, IATA) VALUES (?, ?)");
			_ps.setString(2, a.getIATA());
			for (Iterator i = a.getAirlineCodes().iterator(); i.hasNext(); ) {
				String aCode = (String) i.next();
				_ps.setString(1, aCode);
				_ps.addBatch();
			}
			
			// Execute and commit
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Airport in the Schedule.
	 * @param a the Airport bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Airport a) throws DAOException {
		try {
			startTransaction();
			
			// Update the airport data
			prepareStatement("UPDATE common.AIRPORTS SET ICAO=?, TZ=?, NAME=?, LATITUDE=?, LONGITUDE=? WHERE "
					+ "(IATA=?)");
			_ps.setString(1, a.getICAO());
			_ps.setString(2, a.getTZ().getID());
			_ps.setString(3, a.getName());
			_ps.setDouble(4, a.getLatitude());
			_ps.setDouble(5, a.getLongitude());
			_ps.setString(6, a.getIATA());
			executeUpdate(1);
			
			// Clear out the airlines
			prepareStatement("DELETE FROM common.AIRPORT_AIRLINE WHERE (IATA=?)");
			_ps.setString(1, a.getIATA());
			executeUpdate(0);
			
			// Write the airline data
			prepareStatement("INSERT INTO common.AIRPORT_AIRLINE (CODE, IATA) VALUES (?, ?)");
			_ps.setString(2, a.getIATA());
			for (Iterator i = a.getAirlineCodes().iterator(); i.hasNext(); ) {
				String aCode = (String) i.next();
				_ps.setString(1, aCode);
				_ps.addBatch();
			}
			
			// Execute and commit
			_ps.executeBatch();
			_ps.close();
			commitTransaction();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes an Airport from the Schedule. This operation may fail if there are any Flight Reports or
	 * Events that reference this Airport. In such a case, it is best to remove all Airlines from the Airport.
	 * @param a the Airport bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(Airport a) throws DAOException {
		try {
			prepareStatement("DELETE FROM common.AIRPORTS WHERE (IATA=?)");
			_ps.setString(1, a.getIATA());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}
	
	/**
	 * Adds an entry to the Flight Schedule.
	 * @param entry the Schedule Entry
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(ScheduleEntry entry) throws DAOException {
	   try {
	      prepareStatement("REPLACE INTO SCHEDULE (AIRLINE, FLIGHT, LEG, AIRPORT_D, AIRPORT_A, DISTANCE, "
	            + "EQTYPE, FLIGHT_TIME, TIME_D, TIME_A, HISTORIC, CAN_PURGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
	      _ps.setString(1, entry.getAirline().getCode());
	      _ps.setInt(2, entry.getFlightNumber());
	      _ps.setInt(3, entry.getLeg());
	      _ps.setString(4, entry.getAirportD().getIATA());
	      _ps.setString(5, entry.getAirportA().getIATA());
	      _ps.setInt(6, entry.getDistance());
	      _ps.setString(7, entry.getEquipmentType());
	      _ps.setInt(8, entry.getLength());
	      _ps.setTimestamp(9, createTimestamp(entry.getTimeD()));
	      _ps.setTimestamp(10, createTimestamp(entry.getTimeA()));
	      _ps.setBoolean(11, entry.isHistoric());
	      _ps.setBoolean(12, entry.canPurge());
	            
	      // Update the database
	      executeUpdate(1);
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
	
	/**
	 * Purges entries from the Flight Schedule.
	 * @param force TRUE if all entries should be purged, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge(boolean force) throws DAOException {
	   
	   // Build the SQL statement
	   StringBuilder sqlBuf = new StringBuilder("DELETE FROM SCHEDULE");
	   if (!force)
	      sqlBuf.append(" WHERE (CAN_PURGE=?)");
	   
	   try {
	      prepareStatementWithoutLimits(sqlBuf.toString());
	      if (!force)
	         _ps.setBoolean(1, true);
	      
	      executeUpdate(0);
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
}