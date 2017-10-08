// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.hr.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Acccess Object to read Pilots that have achieved certain accomplishments.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class GetPilotRecognition extends GetPilot {
	
	private static final Cache<CacheableSet<Integer>> _scNomCache = new ExpiringCache<CacheableSet<Integer>>(1, 3600);
	private static final Cache<CacheableLong> _promoCache = new ExpiringCache<CacheableLong>(4, 900);

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPilotRecognition(Connection c) {
		super(c);
	}
	
	/**
	 * Invalidates the Promotion Queue caches.
	 * @param eqType the equipment type, or null to invalidate all
	 */
	public static void invalidate(String eqType) {
		_scNomCache.clear();
		_promoCache.remove("ALL");
		if (eqType != null)
			_promoCache.remove(eqType);
	}
	
	/**
	 * Loads all pilots having achieved a particular Accomplishment.
	 * @param id the Accomplishment database ID
	 * @return a Collection of Pilots
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Pilot> getByAccomplishment(int id) throws DAOException {
		try {
			prepareStatement("SELECT PILOT_ID FROM PILOT_ACCOMPLISHMENTS WHERE (AC_ID=?)");
			_ps.setInt(1, id);
			Collection<Integer> IDs = executeIDs();
			return getByID(IDs, "PILOTS").values();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
    
    /**
     * Returns whether there are Pilots eligible for promotion to Captain in a particular Equipment program.
     * @param eqType the equipment program, or null for all
     * @return the number of pilots
     * @throws DAOException if a JDBC error occurs
     */
    public long hasPromotionQueue(String eqType) throws DAOException {
    	
    	// Remap "all"
    	String eq = (eqType == null) ? "ALL" : eqType; 

    	// Check the cache
    	CacheableLong result = _promoCache.get(eq);
    	if (result != null)
    		return result.getValue();

    	// Get the results
    	Collection<Integer> results = getPromotionQueue(eq);
    	_promoCache.add(new CacheableLong(eq, results.size()));
    	return results.size();
    }
    
    /**
     * Returns Pilots eligible for promotion to Captain.
     * @param eqType the Equipment program, or null for all programs
     * @return a List of Pilot IDs
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<Integer> getPromotionQueue(String eqType) throws DAOException {
    	
    	// Build the SQL statement
        StringBuilder buf = new StringBuilder("SELECT P.ID, P.EQTYPE, (SELECT COUNT(DISTINCT F.ID) FROM PIREPS F, PROMO_EQ PEQ WHERE (F.PILOT_ID=P.ID) AND (F.ID=PEQ.ID) AND (PEQ.EQTYPE=P.EQTYPE) "
       		+ "AND (F.STATUS=?)) AS CLEGS, EQ.C_LEGS, COUNT(DISTINCT EQE.EXAM) AS CREQ_EXAMS, COUNT(DISTINCT EX.ID) as C_EXAMS FROM (PILOTS P, EQTYPES EQ) LEFT JOIN EQEXAMS EQE "
       		+ "ON (EQE.EQTYPE=P.EQTYPE) LEFT JOIN exams.EXAMS EX ON ((EX.PILOT_ID=P.ID) AND (EX.NAME=EQE.EXAM) AND (EQE.EXAMTYPE=?) AND (EX.PASS=?)) WHERE (P.STATUS=?) AND "
       		+ "(P.RANKING=?) AND ((P.EQTYPE=EQ.EQTYPE) AND (EQ.EQTYPE=EQE.EQTYPE) AND (EQE.EXAMTYPE=?)) GROUP BY P.ID HAVING (CLEGS >= EQ.C_LEGS) AND (C_EXAMS>=CREQ_EXAMS)");
        if (eqType != null)
        	buf.append(" AND (P.EQTYPE=?)");
    	
       try {
    	   prepareStatement(buf.toString());
          _ps.setInt(1, FlightReport.OK);
          _ps.setInt(2, Rank.C.ordinal());
          _ps.setBoolean(3, true);
          _ps.setInt(4, Pilot.ACTIVE);
          _ps.setString(5, Rank.FO.getName());
          _ps.setInt(6, Rank.C.ordinal());
          if (eqType != null)
        	  _ps.setString(7, eqType);
          return executeIDs();
       } catch (SQLException se) {
          throw new DAOException(se);
       }
    }
    
    /**
     * Returns Pilots eligible for promotion to Senior Captain.
     * @return a Collection of database IDs
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<Integer> getNominationEligible() throws DAOException {
    	
    	// Check the cache
    	CacheableSet<Integer> results = _scNomCache.get(GetPilotRecognition.class);
    	if (results != null)
    		return results.clone();
    	
    	try {
    		prepareStatementWithoutLimits("SELECT P.ID, (SELECT COUNT(DISTINCT F.ID) FROM PIREPS F WHERE (P.ID=F.PILOT_ID) "
    			+ "AND (F.STATUS=?)) AS LEGS, COUNT(SU.PILOT_ID) AS SC, IFNULL(N.STATUS, ?) AS NOMSTATUS FROM PILOTS P LEFT JOIN "
    			+ "STATUS_UPDATES SU ON (P.ID=SU.PILOT_ID) AND (SU.TYPE=?) LEFT JOIN NOMINATIONS N ON (P.ID=N.ID) AND "
    			+ "(N.QUARTER=?) WHERE (P.RANKING=?) AND (P.STATUS=?) AND (P.CREATED < DATE_SUB(CURDATE(), INTERVAL ? DAY)) "
    			+ "GROUP BY P.ID HAVING (SC=0) AND (NOMSTATUS=?) AND (LEGS>=?)");
    		_ps.setInt(1, FlightReport.OK);
    		_ps.setInt(2, Nomination.Status.PENDING.ordinal());
    		_ps.setInt(3, StatusUpdate.SR_CAPTAIN);
    		_ps.setInt(4, new Quarter().getYearQuarter());
    		_ps.setString(5, Rank.C.getName());
    		_ps.setInt(6, Pilot.ACTIVE);
    		_ps.setInt(7, SystemData.getInt("users.sc.minAge", 90));
    		_ps.setInt(8, Nomination.Status.PENDING.ordinal());
    		_ps.setInt(9, SystemData.getInt("users.sc.minFlights", 5));
    		results = new CacheableSet<Integer>(GetPilotRecognition.class);
    		results.addAll(executeIDs());
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    	
    	_scNomCache.add(results);
    	return results.clone();
    }
}