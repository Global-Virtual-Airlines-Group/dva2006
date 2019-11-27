// Copyright 2005, 2007, 2008, 2009, 2011, 2012, 2013, 2015, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.navdata.*;

/**
 * A Data Access Object to update Navigation data.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class SetNavData extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetNavData(Connection c) {
		super(c);
	}

	/**
	 * Writes an entry to the Navigation Data table.
	 * @param nds a Collection of NavigationDataBeans
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Collection<NavigationDataBean> nds) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.NAVDATA (ITEMTYPE, CODE, LATITUDE, LONGITUDE, FREQ, ALTITUDE, NAME, HDG, LL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ST_PointFromText(?,?))")) {
			for (NavigationDataBean nd : nds) {
				ps.setInt(1, nd.getType().ordinal());
				ps.setString(2, nd.getCode());
				ps.setDouble(3, nd.getLatitude());
				ps.setDouble(4, nd.getLongitude());
				ps.setString(9, formatLocation(nd));
				ps.setInt(10, WGS84_SRID);
				switch (nd.getType()) {
				case VOR:
					VOR vor = (VOR) nd;
					ps.setString(5, vor.getFrequency());
					ps.setInt(6, 0);
					ps.setString(7, vor.getName());
					ps.setInt(8, 0);
					break;

				case NDB:
					NDB ndb = (NDB) nd;
					ps.setString(5, ndb.getFrequency());
					ps.setInt(6, 0);
					ps.setString(7, ndb.getName());
					ps.setInt(8, 0);
					break;

				case AIRPORT:
					AirportLocation al = (AirportLocation) nd;
					ps.setString(5, "-");
					ps.setInt(6, al.getAltitude());
					ps.setString(7, al.getName());
					ps.setInt(8, 0);
					break;

				case RUNWAY:
					Runway rwy = (Runway) nd;
					ps.setString(5, rwy.getFrequency());
					ps.setInt(6, rwy.getLength());
					ps.setString(7, rwy.getName());
					ps.setInt(8, rwy.getHeading());
					break;
					
				default:
					ps.setString(5, "-");
					ps.setInt(6, 0);
					ps.setString(7, "-");
					ps.setInt(8, 0);
				}
				
				ps.addBatch();
			}

			executeUpdate(ps, 1, nds.size());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an Airway entry to the database.
	 * @param a an Airway bean
	 * @throws DAOException if a JDBC error occurs
	 * @see SetNavData#writeRoute(TerminalRoute)
	 */
	public void write(Airway a) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.AIRWAYS (NAME, ID, SEQ, WAYPOINT, WPTYPE, LATITUDE, LONGITUDE, HIGH, LOW, LL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ST_PointFromText(?,?))")) {
			ps.setString(1, a.getCode());
			ps.setInt(2, a.getSequence());
			ps.setBoolean(8, a.isHighLevel());
			ps.setBoolean(9, a.isLowLevel());
			ps.setInt(11, WGS84_SRID);

			// Write the waypoints
			int x = 1;
			for (NavigationDataBean ai : a.getWaypoints()) {
				ps.setInt(3, x + 1);
				ps.setString(4, ai.getCode());
				ps.setInt(5, ai.getType().ordinal());
				ps.setDouble(6, ai.getLatitude());
				ps.setDouble(7, ai.getLongitude());
				ps.setString(10, formatLocation(ai));
				ps.addBatch();
				x++;
			}

			executeUpdate(ps, 1, a.getWaypoints().size());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Writes an SID/STAR entry to the database.
	 * @param tr an TerminalRoute bean
	 * @throws DAOException if a JDBC error occurs
	 * @see SetNavData#write(Airway)
	 */
	public void writeRoute(TerminalRoute tr) throws DAOException {
		try {
			startTransaction();
			
			// Write metadata
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.SIDSTAR_META (ICAO, ID, TYPE, NAME, TRANSITION, RUNWAY, CAN_PURGE) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, tr.getICAO());
				ps.setInt(2, tr.getSequence());
				ps.setInt(3, tr.getType().ordinal());
				ps.setString(4, tr.getName());
				ps.setString(5, tr.getTransition());
				ps.setString(6, tr.getRunway());
				ps.setBoolean(7, tr.getCanPurge());
				executeUpdate(ps, 1);
			}

			// Write the waypoints
			List<NavigationDataBean> wps = tr.getWaypoints();
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.SIDSTAR_WAYPOINTS (ICAO, TYPE, ID, SEQ, WAYPOINT, WPTYPE, LATITUDE, LONGITUDE, LL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ST_PointFromText(?,?))")) {
				ps.setString(1, tr.getICAO());
				ps.setInt(2, tr.getType().ordinal());
				ps.setInt(3, tr.getSequence());
				ps.setInt(10, WGS84_SRID);
				for (int x = 0; x < wps.size(); x++) {
					NavigationDataBean ai = wps.get(x);
					ps.setInt(4, x + 1);
					ps.setString(5, ai.getCode());
					ps.setInt(6, ai.getType().ordinal());
					ps.setDouble(7, ai.getLatitude());
					ps.setDouble(8, ai.getLongitude());
					ps.setString(9, formatLocation(ai));
					ps.addBatch();
				}

				executeUpdate(ps, 1, wps.size());
			}
			
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates Airway waypoint types from the Navigation Data table. This will also load the ICAO region codes.
	 * @return the nuber of entries updated
	 * @throws DAOException if a JDBC error occurs
	 */
	public int updateAirwayWaypoints() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.AIRWAYS A, common.NAVDATA ND SET A.WPTYPE=ND.ITEMTYPE, A.FREQ=ND.FREQ, A.REGION=ND.REGION WHERE (A.WAYPOINT=ND.CODE) AND "
				+ "(ABS(A.LATITUDE-ND.LATITUDE)<0.0001) AND (ABS(A.LONGITUDE-ND.LONGITUDE)<0.0001)")) {
			return executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates Terminal Route waypoint types from the Navigation Data table. This will also load the ICAO region codes.
	 * @return the nuber of entries updated
	 * @throws DAOException if a JDBC error occurs
	 */
	public int updateTRWaypoints() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.SIDSTAR_WAYPOINTS TR, common.NAVDATA ND SET TR.WPTYPE=ND.ITEMTYPE, TR.REGION=ND.REGION WHERE (TR.WAYPOINT=ND.CODE) AND "
				+"(ABS(TR.LATITUDE-ND.LATITUDE)<0.001) AND (ABS(TR.LONGITUDE-ND.LONGITUDE)<0.001)")) {
			return executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Updates the ICAO Region code for navigation data entries.
	 * @param navaidType the navigation aid type
	 * @return the nuber of entries updated
	 * @throws DAOException if a JDBC error occurs
	 */
	public int updateRegions(Navaid navaidType) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.NAVDATA ND, common.NAVREGIONS NR SET ND.REGION=NR.REGION WHERE (ROUND(ND.LATITUDE,0)=NR.LATITUDE) AND "
			+ "(ROUND(ND.LONGITUDE,0)=NR.LONGITUDE) AND (ND.REGION IS NULL) AND (ND.ITEMTYPE=?)")) {
			ps.setInt(1, navaidType.ordinal());
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Copies navigation data from legacy airports.
	 * @param navaidType the navigation aid type
	 * @return the nuber of entries updated
	 * @throws DAOException if a JDBC error occurs
	 */
	public int updateLegacy(Navaid navaidType) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO common.NAVDATA (SELECT * FROM common.NAVLEGACY WHERE (ITEMTYPE=?))")) {
			ps.setInt(1, navaidType.ordinal());
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Terminal Route from the database.
	 * @param tr the TerminalRoute bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(TerminalRoute tr) throws DAOException {
		if (tr == null) return;
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.SIDSTAR_META WHERE (ITEMTYPE=?) AND (ICAO=?) " + "AND (NAME=?) AND (TRANSITION=?) AND (RUNWAY=?)")) {
			ps.setInt(1, tr.getType().ordinal());
			ps.setString(2, tr.getICAO());
			ps.setString(3, tr.getName());
			ps.setString(4, tr.getTransition());
			ps.setString(5, tr.getRunway());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Purges Navigation Aid records from the database.
	 * @param navaidType the navaid type
	 * @return the number of records deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purge(Navaid navaidType) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.NAVDATA WHERE (ITEMTYPE=?)")) {
			ps.setInt(1, navaidType.ordinal());
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Purges Airway records from the database.
	 * @return the number of records deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeAirways() throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("TRUNCATE common.AIRWAYS")) {
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Purges Terminal Route records from the database.
	 * @param routeType the Terminal Route type
	 * @return the number of records deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purgeTerminalRoutes(TerminalRoute.Type routeType) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.SIDSTAR_META WHERE (TYPE=?) AND (CAN_PURGE=?)")) {
			ps.setInt(1, routeType.ordinal());
			ps.setBoolean(2, true);
			return executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}