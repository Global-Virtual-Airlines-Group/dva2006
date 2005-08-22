// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.List;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to obtain user Directory information for Pilots.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetPilotDirectory extends PilotReadDAO {

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPilotDirectory(Connection c) {
		super(c);
	}

	/**
	 * Gets a Pilot based on the directory name. <i>This populates ratings. </i>
	 * @param directoryName the JNDI directory name to search for (eg. cn=Luke Kolin,ou=DVA,o=SCE)
	 * @return the Pilot object, or null if the pilot code was not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public final Pilot getFromDirectory(String directoryName) throws DAOException {
	    try {
	        prepareStatement("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), "
	                + "MAX(F.DATE), S.ID FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) LEFT JOIN "
					+ "SIGNATURES S ON (P.ID=S.ID) WHERE (UPPER(P.LDAP_DN)=?) GROUP BY P.ID");
	        _ps.setInt(1, FlightReport.OK);
	        _ps.setString(2, directoryName.toUpperCase());
	        
	        // Execute the query and get the result
	 	    List results = execute();
	 	    Pilot result = (results.size() == 0) ? null : (Pilot) results.get(0);
	 	    if (result == null)
	 	        return null;
	 	
	 	    // Add roles/ratings
	 	    addRatings(result, SystemData.get("airline.db"));
	 	    addRoles(result, SystemData.get("airline.db"));
	 	
	 	    // Add the result to the cache and return
	 	    _cache.add(result);
	 	    return result;
	    } catch (SQLException se) {
	        throw new DAOException(se);
	    }
	}
	
	/**
	 * Returns the Directory Name for a particular pilot Code.
	 * @param pilotCode the Pilot Code (eg DVA043)
	 * @return the Directory Name of the Pilot, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public String getDirectoryName(String pilotCode) throws DAOException {
		
		// Parse the pilot code
		StringBuffer code = new StringBuffer();
		for (int x = 0; x < pilotCode.length(); x++) {
			char c = pilotCode.charAt(x);
			if (Character.isDigit(c))
				code.append(c);
		}
		
		// If we have no numbers, then abort
		if (code.length() == 0)
			return null;
		
		try {
			prepareStatement("SELECT LDAP_DN FROM PILOTS WHERE (PILOT_ID=?)");
			_ps.setInt(1, Integer.parseInt(code.toString()));
			
			// Execute the query and get return value
			ResultSet rs = _ps.executeQuery();
			String dN = rs.next() ? rs.getString(1) : null;
			
			// Clean up and return
			rs.close();
			_ps.close();
			return dN;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns the Directory Name for a Pilot.
	 * @param fName the Pilot's First (given) name
	 * @param lName the Pilot's Last (family) name
	 * @return the Pilot's Directory Name, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public String getDirectoryName(String fName, String lName) throws DAOException {
		try {
			prepareStatement("SELECT LDAP_DN FROM PILOTS WHERE (FIRSTNAME=?) AND (LASTNAME=?)");
			_ps.setString(1, fName);
			_ps.setString(2, lName);
			
			// Execute the query and get return value
			ResultSet rs = _ps.executeQuery();
			String dN = rs.next() ? rs.getString(1) : null;
			
			// Clean up and return
			rs.close();
			_ps.close();
			return dN;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
   /**
    * Checks if a Person is unique, by checking the first/last names and the e-mail address.
    * @param p the Person
    * @param dbName the database to search
    * @return the number of matches
    * @throws DAOException if a JDBC error occurs
    */
	public int checkUnique(Person p, String dbName) throws DAOException {
	   
	   // Build the SQL statement
	   StringBuffer sqlBuf = new StringBuffer("SELECT COUNT(*) FROM ");
	   sqlBuf.append(dbName.toLowerCase());
	   sqlBuf.append(".PILOTS WHERE (((FIRSTNAME=?) AND (LASTNAME=?)) OR (EMAIL=?)) GROUP BY ID");
	   
      try {
         prepareStatement(sqlBuf.toString());
         _ps.setString(1, p.getFirstName());
         _ps.setString(2, p.getLastName());
         _ps.setString(3, p.getEmail());

         // Execute the query
         ResultSet rs = _ps.executeQuery();
         int resultCount = (rs.next()) ? rs.getInt(1) : 0;
         
         // Clean up and return
         rs.close();
         _ps.close();
         return resultCount;
      } catch (SQLException se) {
         throw new DAOException(se);
      }
   }
}