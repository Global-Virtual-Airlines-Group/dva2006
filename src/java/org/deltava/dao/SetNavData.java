// Copyright 2005, 2007, 2008, 2009, 2011, 2012, 2013, 2015, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.navdata.*;

/**
 * A Data Access Object to update Navigation data.
 * @author Luke
 * @version 8.3
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
	 * @param ndata the NavigationDataBean to write
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(NavigationDataBean ndata) throws DAOException {
		try {
			// Prepare the statement if not already done
			if ((_ps == null) || _ps.isClosed())
				prepareStatement("INSERT INTO common.NAVDATA (ITEMTYPE, CODE, LATITUDE, LONGITUDE, FREQ, ALTITUDE, NAME, HDG, LL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ST_PointFromText(?,?))");

			_ps.setInt(1, ndata.getType().ordinal());
			_ps.setString(2, ndata.getCode());
			_ps.setDouble(3, ndata.getLatitude());
			_ps.setDouble(4, ndata.getLongitude());
			_ps.setString(9, formatLocation(ndata));
			_ps.setInt(10, WGS84_SRID);
			switch (ndata.getType()) {
			case VOR:
				VOR vor = (VOR) ndata;
				_ps.setString(5, vor.getFrequency());
				_ps.setInt(6, 0);
				_ps.setString(7, vor.getName());
				_ps.setInt(8, 0);
				break;

			case NDB:
				NDB ndb = (NDB) ndata;
				_ps.setString(5, ndb.getFrequency());
				_ps.setInt(6, 0);
				_ps.setString(7, ndb.getName());
				_ps.setInt(8, 0);
				break;

			case AIRPORT:
				AirportLocation al = (AirportLocation) ndata;
				_ps.setString(5, "-");
				_ps.setInt(6, al.getAltitude());
				_ps.setString(7, al.getName());
				_ps.setInt(8, 0);
				break;

			case RUNWAY:
				Runway rwy = (Runway) ndata;
				_ps.setString(5, rwy.getFrequency());
				_ps.setInt(6, rwy.getLength());
				_ps.setString(7, rwy.getName());
				_ps.setInt(8, rwy.getHeading());
				break;

			default:
				_ps.setString(5, "-");
				_ps.setInt(6, 0);
				_ps.setString(7, "-");
				_ps.setInt(8, 0);
			}

			_ps.executeUpdate();
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
		try {
			prepareStatement("INSERT INTO common.AIRWAYS (NAME, ID, SEQ, WAYPOINT, WPTYPE, LATITUDE, LONGITUDE, HIGH, LOW, LL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ST_PointFromText(?,?))");
			_ps.setString(1, a.getCode());
			_ps.setInt(2, a.getSequence());
			_ps.setBoolean(8, a.isHighLevel());
			_ps.setBoolean(9, a.isLowLevel());
			_ps.setInt(11, WGS84_SRID);

			// Write the waypoints
			int x = 1;
			for (NavigationDataBean ai : a.getWaypoints()) {
				_ps.setInt(3, x + 1);
				_ps.setString(4, ai.getCode());
				_ps.setInt(5, ai.getType().ordinal());
				_ps.setDouble(6, ai.getLatitude());
				_ps.setDouble(7, ai.getLongitude());
				_ps.setString(10, formatLocation(ai));
				_ps.addBatch();
				x++;
			}

			executeBatchUpdate(1, a.getWaypoints().size());
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
			prepareStatement("REPLACE INTO common.SIDSTAR_META (ICAO, ID, TYPE, NAME, TRANSITION, RUNWAY, CAN_PURGE) VALUES (?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, tr.getICAO());
			_ps.setInt(2, tr.getSequence());
			_ps.setInt(3, tr.getType().ordinal());
			_ps.setString(4, tr.getName());
			_ps.setString(5, tr.getTransition());
			_ps.setString(6, tr.getRunway());
			_ps.setBoolean(7, tr.getCanPurge());
			executeUpdate(1);

			// Write the waypoints
			List<NavigationDataBean> wps = tr.getWaypoints();
			prepareStatementWithoutLimits("INSERT INTO common.SIDSTAR_WAYPOINTS (ICAO, TYPE, ID, SEQ, WAYPOINT, WPTYPE, LATITUDE, LONGITUDE, LL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ST_PointFromText(?,?))");
			_ps.setString(1, tr.getICAO());
			_ps.setInt(2, tr.getType().ordinal());
			_ps.setInt(3, tr.getSequence());
			_ps.setInt(10, WGS84_SRID);
			for (int x = 0; x < wps.size(); x++) {
				NavigationDataBean ai = wps.get(x);
				_ps.setInt(4, x + 1);
				_ps.setString(5, ai.getCode());
				_ps.setInt(6, ai.getType().ordinal());
				_ps.setDouble(7, ai.getLatitude());
				_ps.setDouble(8, ai.getLongitude());
				_ps.setString(9, formatLocation(ai));
				_ps.addBatch();
			}

			executeBatchUpdate(1, wps.size());
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
		try {
			prepareStatementWithoutLimits("UPDATE common.AIRWAYS A, common.NAVDATA ND SET A.WPTYPE=ND.ITEMTYPE, A.FREQ=ND.FREQ, A.REGION=ND.REGION WHERE (A.WAYPOINT=ND.CODE) AND "
				+ "(ABS(A.LATITUDE-ND.LATITUDE)<0.0001) AND (ABS(A.LONGITUDE-ND.LONGITUDE)<0.0001)");
			return executeUpdate(1);
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
		try {
			prepareStatementWithoutLimits("UPDATE common.SIDSTAR_WAYPOINTS TR, common.NAVDATA ND SET TR.WPTYPE=ND.ITEMTYPE, TR.REGION=ND.REGION WHERE (TR.WAYPOINT=ND.CODE) AND "
				+"(ABS(TR.LATITUDE-ND.LATITUDE)<0.001) AND (ABS(TR.LONGITUDE-ND.LONGITUDE)<0.001)");
			return executeUpdate(1);
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
		try {
			prepareStatementWithoutLimits("UPDATE common.NAVDATA ND, common.NAVREGIONS NR SET ND.REGION=NR.REGION WHERE (ROUND(ND.LATITUDE,0)=NR.LATITUDE) AND (ROUND(ND.LONGITUDE,0)=NR.LONGITUDE) "
				+ "AND (ND.REGION IS NULL) AND (ND.ITEMTYPE=?)");
			_ps.setInt(1, navaidType.ordinal());
			return executeUpdate(0);
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
		try {
			prepareStatementWithoutLimits("REPLACE INTO common.NAVDATA (SELECT * FROM common.NAVLEGACY WHERE (ITEMTYPE=?))");
			_ps.setInt(1, navaidType.ordinal());
			return executeUpdate(0);
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
		if (tr == null)
			return;
		try {
			prepareStatementWithoutLimits("DELETE FROM common.SIDSTAR_META WHERE (ITEMTYPE=?) AND (ICAO=?) " + "AND (NAME=?) AND (TRANSITION=?) AND (RUNWAY=?)");
			_ps.setInt(1, tr.getType().ordinal());
			_ps.setString(2, tr.getICAO());
			_ps.setString(3, tr.getName());
			_ps.setString(4, tr.getTransition());
			_ps.setString(5, tr.getRunway());
			executeUpdate(1);
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
		try {
			prepareStatementWithoutLimits("DELETE FROM common.NAVDATA WHERE (ITEMTYPE=?)");
			_ps.setInt(1, navaidType.ordinal());
			return executeUpdate(0);
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
		try {
			prepareStatementWithoutLimits("TRUNCATE common.AIRWAYS");
			return executeUpdate(0);
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
		try {
			prepareStatementWithoutLimits("DELETE FROM common.SIDSTAR_META WHERE (TYPE=?) AND (CAN_PURGE=?)");
			_ps.setInt(1, routeType.ordinal());
			_ps.setBoolean(2, true);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}