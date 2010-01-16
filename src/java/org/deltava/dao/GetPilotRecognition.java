// Copyright 2005, 2006, 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;

import org.deltava.util.cache.*;

/**
 * A Data Acccess Object to read Pilots that have achieved certain accomplishments.
 * @author Luke
 * @version 2.8
 * @since 1.0
 */

public class GetPilotRecognition extends PilotReadDAO {
	
	private static final Cache<CacheableLong> _promoCache = new ExpiringCache<CacheableLong>(4, 900);

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPilotRecognition(Connection c) {
		super(c);
	}
	
	/**
	 * Invalidates a portion of the Promotion Queue cache.
	 * @param eqType the equipment type
	 */
	public static void invalidate(String eqType) {
		_promoCache.remove(eqType);
		_promoCache.remove("ALL");
	}
	
	/**
	 * Returns cache information.
	 */
	public CacheInfo getCacheInfo() {
		CacheInfo info = super.getCacheInfo();
		info.add(_promoCache);
		return info;
	}

    /**
     * Returns members of the &quot;Century Club&quot;, with over 100 Flight Legs.
     * @return the Pilot, or null if not found
     * @throws DAOException if a JDBC error occurs
     */
    public List<Pilot> getCenturyClub() throws DAOException {
        try {
            prepareStatementWithoutLimits("SELECT P.*, COUNT(DISTINCT F.ID) AS LEGS, SUM(F.DISTANCE), ROUND(SUM(F.FLIGHT_TIME), 1), " +
                    "MAX(F.DATE) FROM PILOTS P LEFT JOIN PIREPS F ON ((P.ID=F.PILOT_ID) AND (F.STATUS=?)) GROUP BY P.ID " +
                    "HAVING (LEGS >= 100) ORDER BY LEGS DESC");
            _ps.setInt(1, FlightReport.OK);
            return execute();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Returns wether there are Pilots eligible for promotion to Captain in a particular Equipment program.
     * @param eqType the equipment program, or null for all
     * @return the number of pilots
     * @throws DAOException if a JDBC error occurs
     */
    public long hasPromotionQueue(String eqType) throws DAOException {
    	
    	// Remap "all"
    	if (eqType == null)
    		eqType = "ALL";
    	
    	// Check the cache
    	CacheableLong result = _promoCache.get(eqType);
    	if (result != null)
    		return result.getValue();

    	// Get the results
    	Collection<Integer> results = getPromotionQueue(eqType);
    	_promoCache.add(new CacheableLong(eqType, results.size()));
    	return results.size();
    }
    
    /**
     * Returns Pilots eligible for promotion to Captain.
     * @return a List of Pilot IDs
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<Integer> getPromotionQueue(String eqType) throws DAOException {
    	
    	// Build the SQL statement
        StringBuilder buf = new StringBuilder("SELECT P.ID, (SELECT COUNT(DISTINCT F.ID) FROM PIREPS F, "
        		+ "PROMO_EQ PEQ WHERE (F.PILOT_ID=P.ID) AND (F.ID=PEQ.ID) AND (PEQ.EQTYPE=P.EQTYPE) "
        		+ "AND (F.STATUS=?)) AS CLEGS, EQ.C_LEGS, COUNT(DISTINCT EQE.EXAM) AS CREQ_EXAMS, "
        		+ "COUNT(DISTINCT EX.ID) as C_EXAMS FROM (PILOTS P, EQTYPES EQ) LEFT JOIN EQEXAMS EQE "
        		+ "ON (EQE.EQTYPE=P.EQTYPE) LEFT JOIN exams.EXAMS EX ON ((EX.PILOT_ID=P.ID) AND "
        		+ "(EX.NAME=EQE.EXAM) AND (EQE.EXAMTYPE=?) AND (EX.PASS=?)) WHERE (P.STATUS=?) AND "
        		+ "(P.RANK=?) AND ((P.EQTYPE=EQ.EQTYPE) AND (EQ.EQTYPE=EQE.EQTYPE) AND (EQE.EXAMTYPE=?)) ");
        if (eqType != null)
        	buf.append("AND (P.EQTYPE=?) ");
        buf.append("GROUP BY P.ID HAVING (CLEGS >= EQ.C_LEGS) AND (C_EXAMS>=CREQ_EXAMS)");
    	
       try {
    	   prepareStatement(buf.toString());
          _ps.setInt(1, FlightReport.OK);
          _ps.setInt(2, EquipmentType.EXAM_CAPT);
          _ps.setBoolean(3, true);
          _ps.setInt(4, Pilot.ACTIVE);
          _ps.setString(5, Ranks.RANK_FO);
          _ps.setInt(6, EquipmentType.EXAM_CAPT);
          if (eqType != null)
        	  _ps.setString(7, eqType);
          return executeIDs();
       } catch (SQLException se) {
          throw new DAOException(se);
       }
    }
}