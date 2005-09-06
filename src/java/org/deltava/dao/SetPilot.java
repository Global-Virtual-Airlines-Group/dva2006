// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to support updating Pilot profiles.
 * @author Luke
 * @version 1.0
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
	 * Deletes a Pilot from the database.
	 * @param id the Pilot database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("DELETE FROM PILOTS WHERE (ID=?)");
			_ps.setInt(1, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Marks a Pilot as &quot;On Leave&quot;.
	 * @param id the Pilot database ID
	 * @throws DAOException if a JDBC erorr occurs
	 */
	public void onLeave(int id) throws DAOException {
		
		try {
			prepareStatementWithoutLimits("UPDATE PILOTS SET STATUS=? WHERE (ID=?)");
			_ps.setInt(1, Pilot.ON_LEAVE);
			_ps.setInt(2, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Marks a Pilot as Transferred
	 * @param id, pilot database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	
	public void setTransferred(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("UPDATE PILOTS SET STATUS=? WHERE (ID=?)");
			_ps.setInt(1, Pilot.TRANSFERRED);
			_ps.setInt(2, id);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
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
	   StringBuffer sqlBuf = new StringBuffer("UPDATE ");
	   sqlBuf.append(db.toLowerCase());
	   sqlBuf.append(".PILOTS SET EMAIL=?, LOCATION=?, LEGACY_HOURS=?, HOME_AIRPORT=?, VATSIM_ID=?, " +
	         	"IVAO_ID=?, TZ=?, FILE_NOTIFY=?, EVENT_NOTIFY=?, NEWS_NOTIFY=?, SHOW_EMAIL=?, " +
	            "SHOW_WC_SIG=?, SHOW_WC_SSHOTS=?, UISCHEME=?, DFORMAT=?, TFORMAT=?, NFORMAT=?, " +
	             "AIRPORTCODE=?, MAPTYPE=?, IMHANDLE=?, RANK=?, EQTYPE=?, STATUS=? WHERE (ID=?)");
	   
	    try {
	        // This involves a lot of reads and writes, so its written as a single transaction
	        startTransaction();
	        prepareStatementWithoutLimits(sqlBuf.toString()); 
	        _ps.setString(1, p.getEmail());
	        _ps.setString(2, p.getLocation());
	        _ps.setDouble(3, p.getLegacyHours());
	        _ps.setString(4, p.getHomeAirport());
	        _ps.setString(5, (String) p.getNetworkIDs().get("VATSIM"));
	        _ps.setString(6, (String) p.getNetworkIDs().get("IVAO"));
	        _ps.setString(7, p.getTZ().getID());
	        _ps.setBoolean(8, p.getNotifyOption(Person.FLEET));
	        _ps.setBoolean(9, p.getNotifyOption(Person.EVENT));
	        _ps.setBoolean(10, p.getNotifyOption(Person.NEWS));
	        _ps.setInt(11, p.getEmailAccess());
	        _ps.setBoolean(12, p.getShowSignatures());
	        _ps.setBoolean(13, p.getShowSSThreads());
	        _ps.setString(14, p.getUIScheme());
	        _ps.setString(15, p.getDateFormat());
	        _ps.setString(16, p.getTimeFormat());
	        _ps.setString(17, p.getNumberFormat());
	        _ps.setInt(18, p.getAirportCodeType());
	        _ps.setInt(19, p.getMapType());
	        _ps.setString(20, p.getIMHandle());
	        _ps.setString(21, p.getRank());
	        _ps.setString(22, p.getEquipmentType());
	        _ps.setInt(23, p.getStatus());
	        _ps.setInt(24, p.getID());
	        executeUpdate(1);
	        
		    // Update the roles/ratings
		    writeRoles(p, db);
		    writeRatings(p, db);
		    
		    // Commit the changes and update the cache
		    commitTransaction();
		    PilotReadDAO._cache.add(p);
	    } catch (SQLException se) {
	        rollbackTransaction();
	        throw new DAOException(se);
	    }
	}

	/**
	 * Updates this Pilot's location for the member board.
	 * @param pilotID the Pilot's database ID
	 * @param loc the Pilot's location
	 * @throws DAOException if a JDBC error occurs
	 */
	public void setLocation(int pilotID, GeoLocation loc) throws DAOException {
		try {
			prepareStatement("REPLACE INTO PILOT_MAP (ID, LAT, LNG) VALUES (?, ?, ?)");
			_ps.setInt(1, pilotID);
			_ps.setDouble(2, loc.getLatitude());
			_ps.setDouble(3, loc.getLongitude());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Clears this Pilot's locatoin.
	 * @param pilotID the Pilot's database ID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void clearLocation(int pilotID) throws DAOException {
		try {
			prepareStatement("DELETE FROM PILOT_MAP WHERE (ID=?)");
			_ps.setInt(1, pilotID);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Assigns a Pilot ID to a Pilot.
	 * @param p the Pilot bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void assignID(Pilot p) throws DAOException {
	   try {
	      prepareStatement("UPDATE PILOTS SET PILOT_ID=(MAX(PILOT_ID) + 1) WHERE (ID=?) AND (PILOT_ID=0)");
	      _ps.setInt(1, p.getID());
	      executeUpdate(1);
	      
	      // Invalidate the cache entry
	      PilotReadDAO._cache.remove(p.cacheKey());
	   } catch (SQLException se) {
	      throw new DAOException(se);
	   }
	}
}