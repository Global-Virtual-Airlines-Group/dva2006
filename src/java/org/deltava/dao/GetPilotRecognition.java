// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;

import org.deltava.util.cache.*;

/**
 * A Data Acccess Object to read Pilots that have achieved certain accomplishments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetPilotRecognition extends PilotReadDAO {
	
	private static final Cache<CacheableInteger> _promoCache = new ExpiringCache<CacheableInteger>(4, 900);

	/**
	 * Initialize the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetPilotRecognition(Connection c) {
		super(c);
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
    public int hasPromotionQueue(String eqType) throws DAOException {
    	
    	// Remap "all"
    	if (eqType == null)
    		eqType = "ALL";
    	
    	// Check the cache
    	CacheableInteger result = _promoCache.get(eqType);
    	if (result != null)
    		return result.getValue();

    	// Get the results
    	Collection<Integer> results = getPromotionQueue();
    	if (!"ALL".equals(eqType)) {
    		Collection<Pilot> pilots = getByID(results, "PILOTS").values();
    		for (Iterator<Pilot> i = pilots.iterator(); i.hasNext(); ) {
    			Pilot p = i.next();
    			if (!p.getEquipmentType().equals(eqType))
    				i.remove();
    		}
    	}
    	
    	// Save the result
    	_promoCache.add(new CacheableInteger(eqType, results.size()));
    	return results.size();
    }
    
    /**
     * Returns Pilots eligible for promotion to Captain.
     * @return a List of Pilots
     * @throws DAOException if a JDBC error occurs
     */
    public Collection<Integer> getPromotionQueue() throws DAOException {
       try {
          prepareStatement("SELECT P.ID, (SELECT COUNT(DISTINCT F.ID) FROM PIREPS F, PROMO_EQ PEQ WHERE "
        		  + "(F.PILOT_ID=P.ID) AND (F.ID=PEQ.ID) AND (PEQ.EQTYPE=P.EQTYPE) AND (F.STATUS=?)) AS CLEGS, "
        		  + "EQ.C_LEGS, COUNT(DISTINCT EQE.EXAM) AS CREQ_EXAMS, COUNT(DISTINCT EX.ID) as C_EXAMS FROM "
        		  + "(PILOTS P, EQTYPES EQ) LEFT JOIN EQEXAMS EQE ON (EQE.EQTYPE=P.EQTYPE) LEFT JOIN exams.EXAMS "
        		  + "EX ON ((EX.PILOT_ID=P.ID) AND (EX.NAME=EQE.EXAM) AND (EQE.EXAMTYPE=?) AND (EX.PASS=?)) "
        		  + "WHERE (P.STATUS=?) AND (P.RANK=?) AND ((P.EQTYPE=EQ.EQTYPE) AND (EQ.EQTYPE=EQE.EQTYPE) "
        		  + "AND (EQE.EXAMTYPE=?)) GROUP BY P.ID HAVING (CLEGS >= EQ.C_LEGS) AND (C_EXAMS>=CREQ_EXAMS)");
          _ps.setInt(1, FlightReport.OK);
          _ps.setInt(2, EquipmentType.EXAM_CAPT);
          _ps.setBoolean(3, true);
          _ps.setInt(4, Pilot.ACTIVE);
          _ps.setString(5, Ranks.RANK_FO);
          _ps.setInt(6, EquipmentType.EXAM_CAPT);
          
          // Execute the query
          Collection<Integer> results = new LinkedHashSet<Integer>();
          ResultSet rs = _ps.executeQuery();
          while (rs.next())
        	  results.add(new Integer(rs.getInt(1)));
          
          // Clean up
          rs.close();
          _ps.close();
          return results;
       } catch (SQLException se) {
          throw new DAOException(se);
       }
    }
}