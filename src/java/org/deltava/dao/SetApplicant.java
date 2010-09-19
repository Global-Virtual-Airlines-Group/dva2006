// Copyright 2005, 2006, 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Map;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write Applicants to the database.
 * @author Luke
 * @version 3.3
 * @since 1.0
 */

public class SetApplicant extends PilotWriteDAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetApplicant(Connection c) {
		super(c);
	}

	/**
	 * Marks an Applicant as Rejected.
	 * @param a the Applicant object
	 * @throws DAOException if a JDBC error occurs
	 */
	public void reject(Applicant a) throws DAOException {
		invalidate(a.getID());
		try {
			prepareStatement("UPDATE APPLICANTS SET STATUS=? WHERE (ID=?)");
			_ps.setInt(1, Applicant.REJECTED);
			_ps.setInt(2, a.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an Applicant to the database.
	 * @param a the Applicant object
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Applicant a) throws DAOException {
		invalidate(a.getID());
		try {
			startTransaction();

			// Prepare an INSERT or UPDATE statement
			if (a.getID() == 0) {
				// Write the USERDATA Object
				prepareStatement("INSERT INTO common.USERDATA (AIRLINE, TABLENAME) VALUES (?, ?)");
				_ps.setString(1, SystemData.get("airline.code"));
				_ps.setString(2, "APPLICANTS");
				executeUpdate(1);

				// Get the new applicant ID
				a.setID(getNewID());
				prepareStatement("INSERT INTO APPLICANTS (STATUS, FIRSTNAME, LASTNAME, EMAIL, LOCATION, IMHANDLE, "
						+ "MSNHANDLE, VATSIM_ID, IVAO_ID, LEGACY_HOURS, LEGACY_URL, LEGACY_OK, HOME_AIRPORT, FLEET_NOTIFY, "
						+ "EVENT_NOTIFY, NEWS_NOTIFY, PIREP_NOTIFY, SHOW_EMAIL, CREATED, REGHOSTNAME, REGADDR, "
						+ "DFORMAT, TFORMAT, NFORMAT, AIRPORTCODE, DISTANCEUNITS, SIM_VERSION, TZ, UISCHEME, COMMENTS, "
						+ "HR_COMMENTS, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, INET_ATON(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				_ps.setString(31, a.getHRComments());
				_ps.setInt(32, a.getID());
			} else {
				// Delete stage choices
				prepareStatementWithoutLimits("DELETE FROM APPLICANT_STAGE_CHOICES WHERE (ID=?)");
				_ps.setInt(1, a.getID());
				executeUpdate(0);
				
				// Create the UPDATE statement
				prepareStatement("UPDATE APPLICANTS SET STATUS=?, FIRSTNAME=?, LASTNAME=?, EMAIL=?, LOCATION=?, "
						+ "IMHANDLE=?, MSNHANDLE=?, VATSIM_ID=?, IVAO_ID=?, LEGACY_HOURS=?, LEGACY_URL=?, LEGACY_OK=?, "
						+ "HOME_AIRPORT=?, FLEET_NOTIFY=?, EVENT_NOTIFY=?, NEWS_NOTIFY=?, PIREP_NOTIFY=?, SHOW_EMAIL=?, "
						+ "CREATED=?, REGHOSTNAME=?, REGADDR=INET_ATON(?), DFORMAT=?, TFORMAT=?, NFORMAT=?, "
						+ "AIRPORTCODE=?, DISTANCEUNITS=?, SIM_VERSION=?, TZ=?, UISCHEME=?, COMMENTS=?, EQTYPE=?, RANK=?, "
						+ "HR_COMMENTS=? WHERE (ID=?)");
				_ps.setString(31, a.getEquipmentType());
				_ps.setString(32, a.getRank().getName());
				_ps.setString(33, a.getHRComments());
				_ps.setInt(34, a.getID());
			}

			// Set the fields
			_ps.setInt(1, a.getStatus());
			_ps.setString(2, a.getFirstName());
			_ps.setString(3, a.getLastName());
			_ps.setString(4, a.getEmail());
			_ps.setString(5, a.getLocation());
			_ps.setString(6, a.getIMHandle(InstantMessage.AIM));
			_ps.setString(7, a.getIMHandle(InstantMessage.MSN));
			_ps.setString(8, a.getNetworkID(OnlineNetwork.VATSIM));
			_ps.setString(9, a.getNetworkID(OnlineNetwork.IVAO));
			_ps.setDouble(10, a.getLegacyHours());
			_ps.setString(11, a.getLegacyURL());
			_ps.setBoolean(12, a.getLegacyVerified());
			_ps.setString(13, a.getHomeAirport());
			_ps.setBoolean(14, a.getNotifyOption(Person.FLEET));
			_ps.setBoolean(15, a.getNotifyOption(Person.EVENT));
			_ps.setBoolean(16, a.getNotifyOption(Person.NEWS));
			_ps.setBoolean(17, a.getNotifyOption(Person.PIREP));
			_ps.setInt(18, a.getEmailAccess());
			_ps.setTimestamp(19, createTimestamp(a.getCreatedOn()));
			_ps.setString(20, a.getRegisterHostName());
			_ps.setString(21, a.getRegisterAddress());
			_ps.setString(22, a.getDateFormat());
			_ps.setString(23, a.getTimeFormat());
			_ps.setString(24, a.getNumberFormat());
			_ps.setInt(25, a.getAirportCodeType().ordinal());
			_ps.setInt(26, a.getDistanceType());
			_ps.setInt(27, a.getSimVersion());
			_ps.setString(28, a.getTZ().getID());
			_ps.setString(29, a.getUIScheme());
			_ps.setString(30, a.getComments());
			executeUpdate(1);
			
			// Write the stage choices
			prepareStatementWithoutLimits("INSERT INTO APPLICANT_STAGE_CHOICES (ID, STAGE, EQTYPE) VALUES (?, ?, ?)");
			_ps.setInt(1, a.getID());
			for (Map.Entry<Long, String> me : a.getTypeChoices().entrySet()) {
				_ps.setInt(2, me.getKey().intValue());
				_ps.setString(3, me.getValue());
				_ps.addBatch();
			}
			
			// Write
			_ps.executeBatch();
			_ps.close();
			
			// Commit
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Marks an Applicant as hired, and updates the Pilot ID.
	 * @param a the Applicant bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void hire(Applicant a) throws DAOException {
		invalidate(a.getID());
		try {
			// Update the applicant status
			prepareStatement("UPDATE APPLICANTS SET STATUS=?, PILOT_ID=?, RANK=?, EQTYPE=? WHERE (ID=?)");
			_ps.setInt(1, Applicant.APPROVED);
			_ps.setInt(2, a.getPilotID());
			_ps.setString(3, a.getRank().getName());
			_ps.setString(4, a.getEquipmentType());
			_ps.setInt(5, a.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes an Applicant from the database. Unlike Pilots, applicants can be deleted. This call updates the
	 * <i>APPLICANTS</i> and <i>common.USERDATA</i> tables.
	 * @param id the Applicant database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		invalidate(id);
		try {
			prepareStatement("DELETE FROM APPLICANTS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}