// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Collection;

import org.deltava.beans.stats.GeocodeResult;

import org.deltava.beans.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to update Pilot profiles.
 * @author Luke
 * @version 6.4
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
		sqlBuf.append(formatDBName(db));
		sqlBuf.append(".PILOTS SET EMAIL=?, LOCATION=?, LEGACY_HOURS=?, HOME_AIRPORT=?, VATSIM_ID=?, "
			+ "IVAO_ID=?, TZ=?, NOTIFY=?, SHOW_EMAIL=?, SHOW_WC_SIG=?, SHOW_WC_SSHOTS=?, "
			+ "SHOW_DEF_SIG=?, SHOW_NEW_POSTS=?, UISCHEME=?, NAVBAR=?, VIEWSIZE=?, DFORMAT=?, "
			+ "TFORMAT=?, NFORMAT=?, AIRPORTCODE=?, DISTANCEUNITS=?, WEIGHTUNITS=?, MAPTYPE=?, "
			+ "RANK=?, EQTYPE=?, STATUS=?, NOEXAMS=?, NOVOICE=?, NOCOOLER=?, NOTIMECOMPRESS=?, "
			+ "ACARS_RESTRICT=?, EMAIL_INVALID=?, UID=?, MOTTO=?, PERMANENT=?, FIRSTNAME=?, LASTNAME=? WHERE (ID=?)");

		try {
			// This involves a lot of reads and writes, so its written as a single transaction
			startTransaction();
			prepareStatementWithoutLimits(sqlBuf.toString());
			_ps.setString(1, p.getEmail());
			_ps.setString(2, p.getLocation());
			_ps.setDouble(3, p.getLegacyHours());
			_ps.setString(4, p.getHomeAirport());
			_ps.setString(5, p.getNetworkID(OnlineNetwork.VATSIM));
			_ps.setString(6, p.getNetworkID(OnlineNetwork.IVAO));
			_ps.setString(7, p.getTZ().getID());
			_ps.setInt(8, p.getNotifyCode());
			_ps.setInt(9, p.getEmailAccess());
			_ps.setBoolean(10, p.getShowSignatures());
			_ps.setBoolean(11, p.getShowSSThreads());
			_ps.setBoolean(12, p.getHasDefaultSignature());
			_ps.setBoolean(13, p.getShowNewPosts());
			_ps.setString(14, p.getUIScheme());
			_ps.setBoolean(15, p.getShowNavBar());
			_ps.setInt(16, p.getViewCount());
			_ps.setString(17, p.getDateFormat());
			_ps.setString(18, p.getTimeFormat());
			_ps.setString(19, p.getNumberFormat());
			_ps.setInt(20, p.getAirportCodeType().ordinal());
			_ps.setInt(21, p.getDistanceType().ordinal());
			_ps.setInt(22, p.getWeightType().ordinal());
			_ps.setInt(23, p.getMapType().ordinal());
			_ps.setString(24, p.getRank().getName());
			_ps.setString(25, p.getEquipmentType());
			_ps.setInt(26, p.getStatus());
			_ps.setBoolean(27, p.getNoExams());
			_ps.setBoolean(28, p.getNoVoice());
			_ps.setBoolean(29, p.getNoCooler());
			_ps.setBoolean(30, p.getNoTimeCompression());
			_ps.setInt(31, p.getACARSRestriction().ordinal());
			_ps.setBoolean(32, p.isInvalid());
			_ps.setString(33, p.getLDAPName());
			_ps.setString(34, p.getMotto());
			_ps.setBoolean(35, p.getIsPermanent());
			_ps.setString(36, p.getFirstName());
			_ps.setString(37, p.getLastName());
			_ps.setInt(38, p.getID());
			executeUpdate(1);

			// Update the roles/ratings
			writeRoles(p.getID(), p.getRoles(), db);
			writeRatings(p.getID(), p.getRatings(), db, true);
			writeIMAddrs(p.getID(), p.getIMHandle(), db, true);
			writeAlias(p.getID(), p.getLDAPName());

			// Commit the changes
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", p.cacheKey());
		}
	}

	/**
	 * Updates a Pilot's location for the member board.
	 * @param pilotID the Pilot's database ID
	 * @param loc the Pilot's location
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setLocation(int pilotID, GeoLocation loc) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO PILOT_MAP (ID, LAT, LNG, H) VALUES (?, ?, ?, ((RAND() * 10) - 5))");
			_ps.setInt(1, pilotID);
			_ps.setDouble(2, loc.getLatitude());
			_ps.setDouble(3, loc.getLongitude());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(pilotID));
		}
	}
	
	/**
	 * Sets the pilot's Home town.
	 * @param pilotID the Pilot's database ID
	 * @param gr the Geocoding results.
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setHomeTown(int pilotID, GeocodeResult gr) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE PILOTS SET LOCATION=? WHERE (ID=?)");
			_ps.setString(1, gr.getCityState());
			_ps.setInt(2, pilotID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", Integer.valueOf(pilotID));
		}
	}

	/**
	 * Clears this Pilot's locatoin.
	 * @param pilotID the Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearLocation(int pilotID) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM PILOT_MAP WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Assigns a Pilot ID to a Pilot.
	 * @param p the Pilot bean
	 * @param db the database name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void assignID(Pilot p, String db) throws DAOException {
		try {
			startTransaction();
			
			// Get the next available Pilot ID
			prepareStatementWithoutLimits("SELECT MAX(PILOT_ID)+1 FROM " + formatDBName(db) + ".PILOTS");
			try (ResultSet rs = _ps.executeQuery()) {
				p.setPilotNumber(rs.next() ? rs.getInt(1) : 1);
			}

			_ps.close();

			// Write the new Pilot ID
			prepareStatement("UPDATE " + formatDBName(db) + ".PILOTS SET PILOT_ID=? WHERE (ID=?) AND ((PILOT_ID=0) OR (PILOT_ID IS NULL))");
			_ps.setInt(1, p.getPilotNumber());
			_ps.setInt(2, p.getID());
			executeUpdate(1);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", p.cacheKey());
		}
	}
	
	/**
	 * Adds equipment ratings to a particular Pilot.
	 * @param p the Pilot bean
	 * @param ratings a Collection of ratings
	 * @throws DAOException if a JDBC error occurs
	 */
	public void addRatings(Pilot p, Collection<String> ratings) throws DAOException {
		try {
			writeRatings(p.getID(), ratings, SystemData.get("airline.db"), false);
		} catch (SQLException se) {
			throw new DAOException(se);
		} finally {
			CacheManager.invalidate("Pilots", p.cacheKey());
		}
	}
}