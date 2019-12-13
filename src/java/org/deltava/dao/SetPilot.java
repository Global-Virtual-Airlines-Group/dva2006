// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 9.0
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
		sqlBuf.append(".PILOTS SET EMAIL=?, LOCATION=?, LEGACY_HOURS=?, HOME_AIRPORT=?, VATSIM_ID=?, IVAO_ID=?, PE_ID=?, TZ=?, NOTIFY=?, SHOW_EMAIL=?, SHOW_WC_SIG=?, SHOW_WC_SSHOTS=?, "
			+ "SHOW_DEF_SIG=?, SHOW_NEW_POSTS=?, UISCHEME=?, NAVBAR=?, VIEWSIZE=?, DFORMAT=?, TFORMAT=?, NFORMAT=?, AIRPORTCODE=?, DISTANCEUNITS=?, WEIGHTUNITS=?, MAPTYPE=?, "
			+ "RANKING=?, EQTYPE=?, STATUS=?, NOEXAMS=?, NOVOICE=?, NOCOOLER=?, NOTIMECOMPRESS=?, ACARS_RESTRICT=?, EMAIL_INVALID=?, UID=?, MOTTO=?, PERMANENT=?, FORGOTTEN=?, PROF_CR=?, "
			+ "FIRSTNAME=?, LASTNAME=? WHERE (ID=?)");

		try {
			startTransaction();
			try (PreparedStatement ps = prepareWithoutLimits(sqlBuf.toString())) {
				ps.setString(1, p.getEmail());
				ps.setString(2, p.getLocation());
				ps.setDouble(3, p.getLegacyHours());
				ps.setString(4, p.getHomeAirport());
				ps.setString(5, p.getNetworkID(OnlineNetwork.VATSIM));
				ps.setString(6, p.getNetworkID(OnlineNetwork.IVAO));
				ps.setString(7, p.getNetworkID(OnlineNetwork.PILOTEDGE));
				ps.setString(8, p.getTZ().getID());
				ps.setInt(9, p.getNotifyCode());
				ps.setInt(10, p.getEmailAccess());
				ps.setBoolean(11, p.getShowSignatures());
				ps.setBoolean(12, p.getShowSSThreads());
				ps.setBoolean(13, p.getHasDefaultSignature());
				ps.setBoolean(14, p.getShowNewPosts());
				ps.setString(15, p.getUIScheme());
				ps.setBoolean(16, p.getShowNavBar());
				ps.setInt(17, p.getViewCount());
				ps.setString(18, p.getDateFormat());
				ps.setString(19, p.getTimeFormat());
				ps.setString(20, p.getNumberFormat());
				ps.setInt(21, p.getAirportCodeType().ordinal());
				ps.setInt(22, p.getDistanceType().ordinal());
				ps.setInt(23, p.getWeightType().ordinal());
				ps.setInt(24, p.getMapType().ordinal());
				ps.setInt(25, p.getRank().ordinal());
				ps.setString(26, p.getEquipmentType());
				ps.setInt(27, p.getStatus());
				ps.setBoolean(28, p.getNoExams());
				ps.setBoolean(29, p.getNoVoice());
				ps.setBoolean(30, p.getNoCooler());
				ps.setBoolean(31, p.getNoTimeCompression());
				ps.setInt(32, p.getACARSRestriction().ordinal());
				ps.setBoolean(33, p.isInvalid());
				ps.setString(34, p.getLDAPName());
				ps.setString(35, p.getMotto());
				ps.setBoolean(36, p.getIsPermanent());
				ps.setBoolean(37, p.getIsForgotten());
				ps.setBoolean(38, p.getProficiencyCheckRides());
				ps.setString(39, p.getFirstName());
				ps.setString(40, p.getLastName());
				ps.setInt(41, p.getID());
				executeUpdate(ps, 1);
			}
			
			// Update the roles/ratings
			writeRoles(p.getID(), p.getRoles(), db);
			writeRatings(p.getID(), p.getRatings(), db, true);
			writeIMAddrs(p.getID(), p.getIMHandle(), db, true);
			writeAlias(p.getID(), p.getPilotCode(), p.getLDAPName());
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO PILOT_MAP (ID, LAT, LNG, H) VALUES (?, ?, ?, ((RAND() * 10) - 5))")) {
			ps.setInt(1, pilotID);
			ps.setDouble(2, loc.getLatitude());
			ps.setDouble(3, loc.getLongitude());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("UPDATE PILOTS SET LOCATION=? WHERE (ID=?)")) {
			ps.setString(1, gr.getCityState());
			ps.setInt(2, pilotID);
			executeUpdate(ps, 0);
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
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM PILOT_MAP WHERE (ID=?)")) {
			ps.setInt(1, pilotID);
			executeUpdate(ps, 0);
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
			String dbName = formatDBName(db);
			startTransaction();
			
			// Get the next available Pilot ID
			try (PreparedStatement ps = prepareWithoutLimits("SELECT MAX(PILOT_ID)+1 FROM " +  dbName + ".PILOTS")) {
				try (ResultSet rs = ps.executeQuery()) {
					p.setPilotNumber(rs.next() ? rs.getInt(1) : 1);
				}
			}

			// Write the new Pilot ID
			try (PreparedStatement ps = prepare("UPDATE " + dbName + ".PILOTS SET PILOT_ID=? WHERE (ID=?) AND ((PILOT_ID=0) OR (PILOT_ID IS NULL))")) {
				ps.setInt(1, p.getPilotNumber());
				ps.setInt(2, p.getID());
				executeUpdate(ps, 1);
			}
			
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