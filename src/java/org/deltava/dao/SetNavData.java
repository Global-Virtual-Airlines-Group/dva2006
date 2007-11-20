// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.navdata.*;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to update Navigation data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetNavData extends DAO {
   
   private static final String[] TABLES = {"NAVDATA", "SID_STAR", "AIRWAYS"};

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
	      prepareStatement("REPLACE INTO common.AIRWAYS (NAME, ROUTE) VALUES (?, ?)");
	      _ps.setString(1, a.getCode());
	      _ps.setString(2, a.getRoute());
	      executeUpdate(1);
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
	      prepareStatement("REPLACE INTO common.SID_STAR (ICAO, TYPE, NAME, TRANSITION, RUNWAY, ROUTE) "
	            + "VALUES (?, ?, ?, ?, ?, ?)");
	      _ps.setString(1, tr.getICAO());
	      _ps.setInt(2, tr.getType());
	      _ps.setString(3, tr.getName());
	      _ps.setString(4, tr.getTransition());
	      _ps.setString(5, tr.getRunway());
	      _ps.setString(6, tr.getRoute());
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
	 * Purges all entries from a Navigation Data table.
	 * @return the number of rows deleted
	 * @throws DAOException if a JDBC error occurs
	 */
	public int purge(String tableName) throws DAOException {
	   
	   // Validate the table name
	   if (StringUtils.arrayIndexOf(TABLES, tableName) == -1)
	      throw new DAOException("Invalid Table - " + tableName);	
	   
		try {
			prepareStatementWithoutLimits("DELETE FROM common." + tableName);
			return executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}