// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.navdata.*;

/**
 * A Data Access Object to update Navigation data.
 * @author Luke
 * @version 2.4
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
			if (_ps == null)
				prepareStatement("INSERT INTO common.NAVDATA (ITEMTYPE, CODE, LATITUDE, LONGITUDE, FREQ, ALTITUDE, NAME, HDG) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

			_ps.setInt(1, ndata.getType());
			_ps.setString(2, ndata.getCode());
			_ps.setDouble(3, ndata.getLatitude());
			_ps.setDouble(4, ndata.getLongitude());
			if (ndata.getType() == NavigationDataBean.VOR) {
				VOR vor = (VOR) ndata;
				_ps.setString(5, vor.getFrequency());
				_ps.setInt(6, 0);
				_ps.setString(7, vor.getName());
				_ps.setInt(8, 0);
			} else if (ndata.getType() == NavigationDataBean.NDB) {
				NDB ndb = (NDB) ndata;
				_ps.setString(5, ndb.getFrequency());
				_ps.setInt(6, 0);
				_ps.setString(7, ndb.getName());
				_ps.setInt(8, 0);
			} else if (ndata.getType() == NavigationDataBean.AIRPORT) {
				AirportLocation al = (AirportLocation) ndata;
				_ps.setString(5, "-");
				_ps.setInt(6, al.getAltitude());
				_ps.setString(7, al.getName());
				_ps.setInt(8, 0);
			} else if (ndata.getType() == NavigationDataBean.RUNWAY) {
				Runway rwy = (Runway) ndata;
				_ps.setString(5, rwy.getFrequency());
				_ps.setInt(6, rwy.getLength());
				_ps.setString(7, rwy.getName());
				_ps.setInt(8, rwy.getHeading());
			} else {
				_ps.setString(5, "-");
				_ps.setInt(6, 0);
				_ps.setString(7, "-");
				_ps.setInt(8, 0);
			}

			// Write to the database - and don't clear the prepared statement
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
		   prepareStatement("INSERT INTO common.AIRWAYS (NAME, ID, SEQ, WAYPOINT, WPTYPE, LATITUDE, LONGITUDE, HIGH, LOW) "
				   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
	      _ps.setString(1, a.getCode());
	      _ps.setInt(2, a.getSequence());
	      _ps.setBoolean(8, a.isHighLevel());
	      _ps.setBoolean(9, a.isLowLevel());
	      
	      // Write the waypoints
	      List<NavigationDataBean> wps = a.getWaypoints();
	      for (int x = 0; x < wps.size(); x++) {
	    	  NavigationDataBean ai = wps.get(x);
	    	  _ps.setInt(3, x + 1);
	    	  _ps.setString(4, ai.getCode());
	    	  _ps.setInt(5, ai.getType());
	    	  _ps.setDouble(6, ai.getLatitude());
	    	  _ps.setDouble(7, ai.getLongitude());
	    	  _ps.addBatch();
	      }

	      // Write and clean up
	      _ps.executeBatch();
	      _ps.close();
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
	      prepareStatement("INSERT INTO common.SID_STAR (ICAO, TYPE, NAME, TRANSITION, RUNWAY, SEQ, WAYPOINT, "
	    		  + "WPTYPE, LATITUDE, LONGITUDE, CAN_PURGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
	      _ps.setString(1, tr.getICAO());
	      _ps.setInt(2, tr.getType());
	      _ps.setString(3, tr.getName());
	      _ps.setString(4, tr.getTransition());
	      _ps.setString(5, tr.getRunway());
	      _ps.setBoolean(11, tr.getCanPurge());
	      
	      // Write the waypoints
	      List<NavigationDataBean> wps = tr.getWaypoints();
	      for (int x = 0; x < wps.size(); x++) {
	    	  NavigationDataBean ai = wps.get(x);
	    	  _ps.setInt(6, x + 1);
	    	  _ps.setString(7, ai.getCode());
	    	  _ps.setInt(8, ai.getType());
	    	  _ps.setDouble(9, ai.getLatitude());
	    	  _ps.setDouble(10, ai.getLongitude());
	    	  _ps.addBatch();
	      }
	      
	      // Write and clean up
	      _ps.executeBatch();
	      _ps.close();
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
	
	/**
	 * Updates Airway waypoint types from the Navigation Data table. This will also load
	 * the ICAO region codes.
	 * @return the nuber of entries updated
	 * @throws DAOException if a JDBC error occurs
	 */
	public int updateAirwayWaypoints() throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE common.AIRWAYS A, common.NAVDATA ND SET A.WPTYPE=ND.ITEMTYPE, "
					+ "A.REGION=ND.REGION WHERE (A.WAYPOINT=ND.CODE) AND (ABS(A.LATITUDE-ND.LATITUDE)<0.0001) AND "
					+ "(ABS(A.LONGITUDE-ND.LONGITUDE)<0.0001)");
			return executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates Terminal Route waypoint types from the Navigation Data table. This will also load
	 * the ICAO region codes.
	 * @return the nuber of entries updated
	 * @throws DAOException if a JDBC error occurs
	 */
	public int updateTRWaypoints() throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE common.SID_STAR TR, common.NAVDATA ND SET TR.WPTYPE=ND.ITEMTYPE, "
					+ "TR.REGION=ND.REGION WHERE (TR.WAYPOINT=ND.CODE) AND (ABS(TR.LATITUDE-ND.LATITUDE) < 0.001) AND "
					+ "(ABS(TR.LONGITUDE-ND.LONGITUDE) < 0.001)");
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
	public int updateRegions(int navaidType) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE common.NAVDATA ND, common.NAVREGIONS NR SET ND.REGION=NR.REGION WHERE "
					+ "(ROUND(ND.LATITUDE,1)=NR.LATITUDE) AND (ROUND(ND.LONGITUDE,1)=NR.LONGITUDE) AND (ND.REGION IS NULL) "
					+ "AND (ND.ITEMTYPE=?)");
			_ps.setInt(1, navaidType);
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
		if (tr == null) return;
		try {
			prepareStatementWithoutLimits("DELETE FROM common.SID_STAR WHERE (ITEMTYPE=?) AND (ICAO=?) "
					+ "AND (NAME=?) AND (TRANSITION=?) AND (RUNWAY=?)");
			_ps.setInt(1, tr.getType());
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
	public int purge(int navaidType) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.NAVDATA WHERE (ITEMTYPE=?)");
			_ps.setInt(1, navaidType);
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
	public int purgeTerminalRoutes(int routeType) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM common.SID_STAR WHERE (TYPE=?) AND (CAN_PURGE=?)");
			_ps.setInt(1, routeType);
			_ps.setBoolean(2, true);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}