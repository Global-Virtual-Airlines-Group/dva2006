// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write Applicants to the database.
 * @author Luke
 * @version 1.0
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
		try {
			prepareStatement("UPDATE APPLICANTS SET STATUS=? WHERE (ID=?)");
			_ps.setInt(1, Applicant.REJECTED);
			_ps.setInt(2, a.getID());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}

		// Update the cache
		_cache.add(a);
	}

	/**
	 * Writes an Applicant to the database.
	 * @param a the Applicant object
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Applicant a) throws DAOException {
		
		invalidate(a);
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
						+ "VATSIM_ID, IVAO_ID, LEGACY_HOURS, LEGACY_URL, LEGACY_OK, HOME_AIRPORT, FLEET_NOTIFY, "
						+ "EVENT_NOTIFY, NEWS_NOTIFY, SHOW_EMAIL, CREATED, REGHOSTNAME, DFORMAT, TFORMAT, NFORMAT, "
						+ "AIRPORTCODE, TZ, UISCHEME, ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				_ps.setInt(25, a.getID());
			} else {
				prepareStatement("UPDATE APPLICANTS SET STATUS=?, FIRSTNAME=?, LASTNAME=?, EMAIL=?, LOCATION=?, "
						+ "IMHANDLE=?, VATSIM_ID=?, IVAO_ID=?, LEGACY_HOURS=?, LEGACY_URL=?, LEGACY_OK=?, "
						+ "HOME_AIRPORT=?, FLEET_NOTIFY=?, EVENT_NOTIFY=?, NEWS_NOTIFY=?, SHOW_EMAIL=?, CREATED=?, "
						+ "REGHOSTNAME=?, DFORMAT=?, TFORMAT=?, NFORMAT=?, AIRPORTCODE=?, TZ=?, UISCHEME=?, EQTYPE=?, "
						+ "RANK=? WHERE (ID=?)");
				_ps.setString(25, a.getEquipmentType());
				_ps.setString(26, a.getRank());
				_ps.setInt(27, a.getID());
			}

			// Set the fields
			_ps.setInt(1, a.getStatus());
			_ps.setString(2, a.getFirstName());
			_ps.setString(3, a.getLastName());
			_ps.setString(4, a.getEmail());
			_ps.setString(5, a.getLocation());
			_ps.setString(6, a.getIMHandle());
			_ps.setString(7, (String) a.getNetworkIDs().get("VATSIM"));
			_ps.setString(8, (String) a.getNetworkIDs().get("IVAO"));
			_ps.setDouble(9, a.getLegacyHours());
			_ps.setString(10, a.getLegacyURL());
			_ps.setBoolean(11, a.getLegacyVerified());
			_ps.setString(12, a.getHomeAirport());
			_ps.setBoolean(13, a.getNotifyOption(Person.FLEET));
			_ps.setBoolean(14, a.getNotifyOption(Person.EVENT));
			_ps.setBoolean(15, a.getNotifyOption(Person.NEWS));
			_ps.setInt(16, a.getEmailAccess());
			_ps.setTimestamp(17, createTimestamp(a.getCreatedOn()));
			_ps.setString(18, a.getRegisterHostName());
			_ps.setString(19, a.getDateFormat());
			_ps.setString(20, a.getTimeFormat());
			_ps.setString(21, a.getNumberFormat());
			_ps.setInt(22, a.getAirportCodeType());
			_ps.setString(23, a.getTZ().getID());
			_ps.setString(24, a.getUIScheme());

			// Update the database and commit
			executeUpdate(1);
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
		
		invalidate(a);
		try {
			// Update the applicant status
			prepareStatement("UPDATE APPLICANTS SET STATUS=?, PILOT_ID=?, RANK=?, EQTYPE=? WHERE (ID=?)");
			_ps.setInt(1, Applicant.APPROVED);
			_ps.setInt(2, a.getPilotID());
			_ps.setString(3, a.getRank());
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
			startTransaction();

			// Remove the applicant profile
			prepareStatement("DELETE FROM APPLICANTS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);

			// Remove the USERDATA object
			prepareStatement("DELETE FROM common.USERDATA WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
}