// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Map;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write Applicants to the database.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepare("UPDATE APPLICANTS SET STATUS=? WHERE (ID=?)")) {
			ps.setInt(1, ApplicantStatus.REJECTED.ordinal());
			ps.setInt(2, a.getID());
			executeUpdate(ps, 1);
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
		try {
			startTransaction();

			// Write the USERDATA Object
			if (a.getID() == 0) {
				try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.USERDATA (AIRLINE, TABLENAME) VALUES (?, ?)")) {
					ps.setString(1, SystemData.get("airline.code"));
					ps.setString(2, "APPLICANTS");
					executeUpdate(ps, 1);
				}
				
				// Get the new applicant ID
				a.setID(getNewID());
			} else {
				// Delete stage choices
				try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM APPLICANT_STAGE_CHOICES WHERE (ID=?)")) {
					ps.setInt(1, a.getID());
					executeUpdate(ps, 0);
				}
			}

			try (PreparedStatement ps = prepare("INSERT INTO APPLICANTS (STATUS, FIRSTNAME, LASTNAME, EMAIL, LOCATION, VATSIM_ID, IVAO_ID, PE_ID, LEGACY_HOURS, LEGACY_URL, LEGACY_OK, HOME_AIRPORT, "
				+ "NOTIFY, SHOW_EMAIL, CREATED, REGHOSTNAME, REGADDR, DFORMAT, TFORMAT, NFORMAT, AIRPORTCODE, DISTANCEUNITS, SIM_VERSION, TZ, UISCHEME, COMMENTS, HR_COMMENTS, ID) VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, INET6_ATON(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE STATUS=VALUES(STATUS), FIRSTNAME=VALUES(FIRSTNAME), LASTNAME=VALUES(LASTNAME), "
				+ "EMAIL=VALUES(EMAIL), LOCATION=VALUES(LOCATION), VATSIM_ID=VALUES(VATSIM_ID), IVAO_ID=VALUES(IVAO_ID), PE_ID=VALUES(PE_ID), LEGACY_HOURS=VALUES(LEGACY_HOURS), LEGACY_URL=VALUES(LEGACY_URL), "
				+ "LEGACY_OK=VALUES(LEGACY_OK), HOME_AIRPORT=VALUES(HOME_AIRPORT), NOTIFY=VALUES(NOTIFY), SHOW_EMAIL=VALUES(SHOW_EMAIL), DFORMAT=VALUES(DFORMAT), TFORMAT=VALUES(TFORMAT), "
				+ "NFORMAT=VALUES(NFORMAT), AIRPORTCODE=VALUES(AIRPORTCODE), DISTANCEUNITS=VALUES(DISTANCEUNITS), SIM_VERSION=VALUES(SIM_VERSION), TZ=VALUES(TZ), UISCHEME=VALUES(UISCHEME), "
				+ "EQTYPE=?, RANKING=?, HR_COMMENTS=VALUES(HR_COMMENTS)")) {
				ps.setInt(1, a.getStatus().ordinal());
				ps.setString(2, a.getFirstName());
				ps.setString(3, a.getLastName());
				ps.setString(4, a.getEmail());
				ps.setString(5, a.getLocation());
				ps.setString(6, a.getNetworkID(OnlineNetwork.VATSIM));
				ps.setString(7, a.getNetworkID(OnlineNetwork.IVAO));
				ps.setString(8, a.getNetworkID(OnlineNetwork.PILOTEDGE));
				ps.setDouble(9, a.getLegacyHours());
				ps.setString(10, a.getLegacyURL());
				ps.setBoolean(11, a.getLegacyVerified());
				ps.setString(12, a.getHomeAirport());
				ps.setInt(13, a.getNotifyCode());
				ps.setInt(14, a.getEmailAccess());
				ps.setTimestamp(15, createTimestamp(a.getCreatedOn()));
				ps.setString(16, a.getRegisterHostName());
				ps.setString(17, a.getRegisterAddress());
				ps.setString(18, a.getDateFormat());
				ps.setString(19, a.getTimeFormat());
				ps.setString(20, a.getNumberFormat());
				ps.setInt(21, a.getAirportCodeType().ordinal());
				ps.setInt(22, a.getDistanceType().ordinal());
				ps.setInt(23, a.getSimVersion().ordinal());
				ps.setString(24, a.getTZ().getID());
				ps.setString(25, a.getUIScheme());
				ps.setString(26, a.getComments());
				ps.setString(27, a.getHRComments());
				ps.setInt(28, a.getID());
				ps.setString(29, a.getEquipmentType());
				ps.setString(30, (a.getRank() == null) ? null : a.getRank().getName());
				executeUpdate(ps, 1);
			}
			
			// Write the stage choices
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO APPLICANT_STAGE_CHOICES (ID, STAGE, EQTYPE) VALUES (?, ?, ?)")) {
				ps.setInt(1, a.getID());
				for (Map.Entry<Long, String> me : a.getTypeChoices().entrySet()) {
					ps.setInt(2, me.getKey().intValue());
					ps.setString(3, me.getValue());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, a.getTypeChoices().size());
			}
			
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
		try (PreparedStatement ps = prepare("UPDATE APPLICANTS SET STATUS=?, PILOT_ID=?, RANKING=?, EQTYPE=? WHERE (ID=?)")) {
			ps.setInt(1, ApplicantStatus.APPROVED.ordinal());
			ps.setInt(2, a.getPilotID());
			ps.setString(3, a.getRank().getName());
			ps.setString(4, a.getEquipmentType());
			ps.setInt(5, a.getID());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("DELETE FROM APPLICANTS WHERE (ID=?)")) {
			ps.setInt(1, id);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}