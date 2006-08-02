// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Collection;

import org.deltava.beans.*;

/**
 * A Data Access Object to write new Pilots to the database.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetPilotTransfer extends SetPilot {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetPilotTransfer(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Pilot to a database.
	 * @param p the Pilot/Applicant
	 * @param dbName the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void transfer(Person p, String dbName, Collection<String> ratings) throws DAOException {

		// Get the database ID
		int id = (p instanceof Applicant) ? ((Applicant) p).getPilotID() : p.getID();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS (FIRSTNAME, LASTNAME, STATUS, LDAP_DN, EMAIL, LOCATION, IMHANDLE, MSNHANDLE, "
						+ "LEGACY_HOURS, HOME_AIRPORT, EQTYPE, RANK, VATSIM_ID, IVAO_ID, CREATED, LOGINS, LAST_LOGIN, "
						+ "LAST_LOGOFF, TZ, FILE_NOTIFY, EVENT_NOTIFY, NEWS_NOTIFY, PIREP_NOTIFY, SHOW_EMAIL, UISCHEME, "
						+ "LOGINHOSTNAME, DFORMAT, TFORMAT, NFORMAT, AIRPORTCODE, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
						+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		try {
			startTransaction();

			// Write the new Pilot object
			prepareStatement(sqlBuf.toString());
			_ps.setString(1, p.getFirstName());
			_ps.setString(2, p.getLastName());
			_ps.setInt(3, Pilot.ACTIVE);
			_ps.setString(4, p.getDN());
			_ps.setString(5, p.getEmail());
			_ps.setString(6, p.getLocation());
			_ps.setString(7, p.getIMHandle(InstantMessage.AIM));
			_ps.setString(8, p.getIMHandle(InstantMessage.MSN));
			_ps.setDouble(9, p.getLegacyHours());
			_ps.setString(10, p.getHomeAirport());
			_ps.setString(11, p.getEquipmentType());
			_ps.setString(12, p.getRank());
			_ps.setString(13, p.getNetworkIDs().get(OnlineNetwork.VATSIM));
			_ps.setString(14, p.getNetworkIDs().get(OnlineNetwork.IVAO));
			_ps.setTimestamp(15, createTimestamp(p.getCreatedOn()));
			_ps.setInt(16, p.getLoginCount());
			_ps.setTimestamp(17, createTimestamp(p.getLastLogin()));
			_ps.setTimestamp(18, createTimestamp(p.getLastLogoff()));
			_ps.setString(19, p.getTZ().getID());
			_ps.setBoolean(20, p.getNotifyOption(Person.FLEET));
			_ps.setBoolean(21, p.getNotifyOption(Person.EVENT));
			_ps.setBoolean(22, p.getNotifyOption(Person.NEWS));
			_ps.setBoolean(23, p.getNotifyOption(Person.PIREP));
			_ps.setInt(24, p.getEmailAccess());
			_ps.setString(25, p.getUIScheme());
			_ps.setString(26, p.getLoginHost());
			_ps.setString(27, p.getDateFormat());
			_ps.setString(28, p.getTimeFormat());
			_ps.setString(29, p.getNumberFormat());
			_ps.setInt(30, p.getAirportCodeType());
			_ps.setInt(31, id);
			executeUpdate(1);

			// Write the ratings - don't bother writing roles
			writeRatings(id, ratings, dbName, false);

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Marks a Pilot as Transferred.
	 * @param id the pilot database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setTransferred(int id) throws DAOException {
		invalidate(id);
		try {
			prepareStatementWithoutLimits("UPDATE PILOTS SET STATUS=? WHERE (ID=?)");
			_ps.setInt(1, Pilot.TRANSFERRED);
			_ps.setInt(2, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}