// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2012, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Collection;

import org.deltava.beans.*;

/**
 * A Data Access Object to transfer pilots between Airlines.
 * @author Luke
 * @version 8.0
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
	 * @param ratings the Pilot's equipment ratings
	 * @throws DAOException if a JDBC error occurs
	 */
	public void transfer(Person p, String dbName, Collection<String> ratings) throws DAOException {

		// Get the database ID
		int id = (p instanceof Applicant) ? ((Applicant) p).getPilotID() : p.getID();

		// Build the SQL statement
		StringBuilder sqlBuf = new StringBuilder("INSERT INTO ");
		sqlBuf.append(formatDBName(dbName));
		sqlBuf.append(".PILOTS (FIRSTNAME, LASTNAME, STATUS, LDAP_DN, EMAIL, LOCATION, LEGACY_HOURS, HOME_AIRPORT, EQTYPE, RANKING, VATSIM_ID, IVAO_ID, PE_ID, CREATED, "
			+ "LOGINS, LAST_LOGIN, LAST_LOGOFF, TZ, NOTIFY, SHOW_EMAIL, UISCHEME, VIEWSIZE, LOGINHOSTNAME, DFORMAT, TFORMAT, NFORMAT, AIRPORTCODE, DISTANCEUNITS, NAVBAR, ID) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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
			_ps.setDouble(7, p.getLegacyHours());
			_ps.setString(8, p.getHomeAirport());
			_ps.setString(9, p.getEquipmentType());
			_ps.setString(10, p.getRank().getName());
			_ps.setString(11, p.getNetworkID(OnlineNetwork.VATSIM));
			_ps.setString(12, p.getNetworkID(OnlineNetwork.IVAO));
			_ps.setString(13, p.getNetworkID(OnlineNetwork.PILOTEDGE));
			_ps.setTimestamp(14, createTimestamp(p.getCreatedOn()));
			_ps.setInt(15, p.getLoginCount());
			_ps.setTimestamp(16, createTimestamp(p.getLastLogin()));
			_ps.setTimestamp(17, createTimestamp(p.getLastLogoff()));
			_ps.setString(18, p.getTZ().getID());
			_ps.setInt(19, p.getNotifyCode());
			_ps.setInt(20, p.getEmailAccess());
			_ps.setString(21, p.getUIScheme());
			_ps.setInt(22, p.getViewCount());
			_ps.setString(23, p.getLoginHost());
			_ps.setString(24, p.getDateFormat());
			_ps.setString(25, p.getTimeFormat());
			_ps.setString(26, p.getNumberFormat());
			_ps.setInt(27, p.getAirportCodeType().ordinal());
			_ps.setInt(28, p.getDistanceType().ordinal());
			_ps.setBoolean(29, (p instanceof Applicant) ? true : ((Pilot) p).getShowNavBar());
			_ps.setInt(30, id);
			executeUpdate(1);

			// Write the ratings - don't bother writing roles
			writeRatings(id, ratings, dbName, false);
			writeIMAddrs(id, p.getIMHandle(), dbName, false);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}