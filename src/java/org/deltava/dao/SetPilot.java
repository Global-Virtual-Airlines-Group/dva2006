// Copyright (c) 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to support updating Pilot profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetPilot extends PilotWriteDAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetPilot(Connection c) {
		super(c);
	}

	/**
	 * Marks a Pilot as &quot;On Leave&quot;.
	 * @param id the Pilot database ID
	 * @throws DAOException if a JDBC erorr occurs
	 */
	public void onLeave(int id) throws DAOException {
		invalidate(id);
		try {
			prepareStatementWithoutLimits("UPDATE PILOTS SET STATUS=? WHERE (ID=?)");
			_ps.setInt(1, Pilot.ON_LEAVE);
			_ps.setInt(2, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Pilot profile in the current database.
	 * @param p the Pilot profile to update
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Pilot p) throws DAOException {
		write(p, SystemData.get("airline.db"));
	}

	/**
	 * Updates an existing Pilot profile.
	 * @param p the Pilot profile to update
	 * @param db the database to write to
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Pilot p, String db) throws DAOException {

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("UPDATE ");
		sqlBuf.append(db.toLowerCase());
		sqlBuf.append(".PILOTS SET EMAIL=?, LOCATION=?, LEGACY_HOURS=?, HOME_AIRPORT=?, VATSIM_ID=?, "
				+ "IVAO_ID=?, TZ=?, FILE_NOTIFY=?, EVENT_NOTIFY=?, NEWS_NOTIFY=?, SHOW_EMAIL=?, "
				+ "SHOW_WC_SIG=?, SHOW_WC_SSHOTS=?, SHOW_DEF_SIG=?, UISCHEME=?, DFORMAT=?, "
				+ "TFORMAT=?, NFORMAT=?, AIRPORTCODE=?, MAPTYPE=?, IMHANDLE=?, RANK=?, EQTYPE=?, "
				+ "STATUS=?, NOEXAMS=?, FIRSTNAME=?, LASTNAME=? WHERE (ID=?)");

		// Invalidate the cache entry
		invalidate(p);

		try {
			// This involves a lot of reads and writes, so its written as a single transaction
			startTransaction();
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, p.getEmail());
			_ps.setString(2, p.getLocation());
			_ps.setDouble(3, p.getLegacyHours());
			_ps.setString(4, p.getHomeAirport());
			_ps.setString(5, p.getNetworkIDs().get("VATSIM"));
			_ps.setString(6, p.getNetworkIDs().get("IVAO"));
			_ps.setString(7, p.getTZ().getID());
			_ps.setBoolean(8, p.getNotifyOption(Person.FLEET));
			_ps.setBoolean(9, p.getNotifyOption(Person.EVENT));
			_ps.setBoolean(10, p.getNotifyOption(Person.NEWS));
			_ps.setInt(11, p.getEmailAccess());
			_ps.setBoolean(12, p.getShowSignatures());
			_ps.setBoolean(13, p.getShowSSThreads());
			_ps.setBoolean(14, p.getHasDefaultSignature());
			_ps.setString(15, p.getUIScheme());
			_ps.setString(16, p.getDateFormat());
			_ps.setString(17, p.getTimeFormat());
			_ps.setString(18, p.getNumberFormat());
			_ps.setInt(19, p.getAirportCodeType());
			_ps.setInt(20, p.getMapType());
			_ps.setString(21, p.getIMHandle());
			_ps.setString(22, p.getRank());
			_ps.setString(23, p.getEquipmentType());
			_ps.setInt(24, p.getStatus());
			_ps.setBoolean(25, p.getNoExams());
			_ps.setString(26, p.getFirstName());
			_ps.setString(27, p.getLastName());
			_ps.setInt(28, p.getID());
			executeUpdate(1);

			// Update the roles/ratings
			writeRoles(p.getID(), p.getRoles(), db);
			writeRatings(p.getID(), p.getRatings(), db, true);

			// Commit the changes and update the cache
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates this Pilot's location for the member board.
	 * @param pilotID the Pilot's database ID
	 * @param loc the Pilot's location
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setLocation(int pilotID, GeoLocation loc) throws DAOException {
		try {
			prepareStatement("REPLACE INTO PILOT_MAP (ID, LAT, LNG) VALUES (?, ?, ?)");
			_ps.setInt(1, pilotID);
			_ps.setDouble(2, loc.getLatitude());
			_ps.setDouble(3, loc.getLongitude());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Clears this Pilot's locatoin.
	 * @param pilotID the Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearLocation(int pilotID) throws DAOException {
		try {
			prepareStatement("DELETE FROM PILOT_MAP WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Assigns a Pilot ID to a Pilot.
	 * @param p the Pilot bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void assignID(Pilot p) throws DAOException {
		invalidate(p);
		try {
			// Get the next available Pilot ID
			prepareStatement("SELECT MAX(PILOT_ID)+1 FROM PILOTS");
			ResultSet rs = _ps.executeQuery();
			p.setPilotNumber(rs.next() ? rs.getInt(1) : 1);

			// Clean up
			rs.close();
			_ps.close();

			// Write the new Pilot ID
			prepareStatement("UPDATE PILOTS SET PILOT_ID=? WHERE (ID=?) AND ((PILOT_ID=0) OR (PILOT_ID IS NULL))");
			_ps.setInt(1, p.getPilotNumber());
			_ps.setInt(2, p.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}