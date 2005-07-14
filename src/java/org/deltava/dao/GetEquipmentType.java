package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.EquipmentType;
import org.deltava.beans.Ranks;

/**
 * A Data Access Object to retrieve equipment type profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetEquipmentType extends DAO {

    /**
     * Initializes the DAO with a given JDBC connection.
     * @param c the JDBC Connection
     */
    public GetEquipmentType(Connection c) {
        super(c);
    }

    /**
     * Returns a particular Equipment Program profile.
     * @param eqType the Equipment Type to return
     * @return the Equipment Type profile
     * @throws DAOException if a JDBC error occurs
     */
    public EquipmentType get(String eqType) throws DAOException {
        try {
            prepareStatement("SELECT EQ.*, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL, R.RATING_TYPE, " +
                    "R.RATED_EQ FROM EQTYPES EQ, PILOTS P, EQRATINGS R WHERE (EQ.CP_ID=P.ID) AND " +
                    "(EQ.EQTYPE=R.EQTYPE) AND (EQ.EQTYPE=?) ORDER BY EQ.STAGE, EQ.EQTYPE");
            _ps.setString(1, eqType);
            setQueryMax(1);
            
            // Execute the query - if we get nothing back, then return null
            List results = execute();
            return (results.size() == 0) ? null : (EquipmentType) results.get(0);
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }

    /**
     * Returns all the Equipment Types in a particular stage.
     * @param stage the stage number
     * @return a List of EquipmentTypes
     * @throws DAOException if a JDBC error occurs
     */
    public List getByStage(int stage) throws DAOException {
        try {
            prepareStatement("SELECT EQ.*, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL, R.RATING_TYPE, " +
                    "R.RATED_EQ FROM EQTYPES EQ, PILOTS P, EQRATINGS R WHERE (EQ.CP_ID=P.ID) AND " +
                    "(EQ.EQTYPE=R.EQTYPE) AND (EQ.STAGE=?) ORDER BY EQ.STAGE, EQ.EQTYPE");
            _ps.setInt(1, stage);
            
            // Return results
            return execute();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Returns all Active Equipment Types.
     * @return a List of EquipmentTypes
     * @throws DAOException if a JDBC error occurs
     */
    public List getActive() throws DAOException {
        try {
            prepareStatement("SELECT EQ.*, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL, R.RATING_TYPE, " +
                    "R.RATED_EQ FROM EQTYPES EQ, PILOTS P, EQRATINGS R WHERE (EQ.CP_ID=P.ID) AND " +
                    "(EQ.EQTYPE=R.EQTYPE) AND (EQ.ACTIVE=?) ORDER BY EQ.STAGE, EQ.EQTYPE");
            _ps.setBoolean(1, true);
            return execute();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Returns all Equipment Types.
     * @return a List of EquipmentTypes
     * @throws DAOException if a JDBC error occurs
     */
    public List getAll() throws DAOException {
        try {
            prepareStatement("SELECT EQ.*, CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME), P.EMAIL, R.RATING_TYPE, " +
                    "R.RATED_EQ FROM EQTYPES EQ, PILOTS P, EQRATINGS R WHERE (EQ.CP_ID=P.ID) AND " +
                    "(EQ.EQTYPE=R.EQTYPE) ORDER BY EQ.STAGE, EQ.EQTYPE");
            return execute();
        } catch (SQLException se) {
            throw new DAOException(se);
        }
    }
    
    /**
     * Returns the Equipment Programs for whom a flight in a given aircraft counts for promotion.
     * @param eqType the Aircraft type
     * @return a Collection of equipment program names
     * @throws DAOException if a JDBC error occurs
     */
    public Collection getPrimaryTypes(String eqType) throws DAOException {
    	try {
    		prepareStatementWithoutLimits("SELECT EQTYPE FROM EQRATINGS WHERE (RATING_TYPE=?) AND (RATED_EQ=?)");
    		_ps.setInt(1, EquipmentType.PRIMARY_RATING);
    		_ps.setString(2, eqType);
    		
    		// Execute the query
    		ResultSet rs = _ps.executeQuery();
    		
    		// Iterate through the results
    		Set results = new HashSet();
    		while (rs.next())
    			results.add(rs.getString(1));

    		// Clean up and return
    		rs.close();
    		_ps.close();
    		return results;
    	} catch (SQLException se) {
    		throw new DAOException(se);
    	}
    }

    /**
     * Helper method to iterate through the result set.
     */
    private List execute() throws SQLException {

       // Execute the query
        ResultSet rs = _ps.executeQuery();

        // Iterate through the results
        List results = new ArrayList();
        EquipmentType eq = new EquipmentType("");
        while (rs.next()) {
            String eqName = rs.getString(1);
            
            // If it's a new equipment type, then add it to the list and populate it 
            if (!eqName.equals(eq.getName())) {
                eq = new EquipmentType(eqName);
                eq.setCPID(rs.getInt(2));
                eq.setStage(rs.getInt(3));
                eq.addRanks(rs.getString(4), ",");
                eq.setExamName(Ranks.RANK_FO, rs.getString(5));
                eq.setExamName(Ranks.RANK_C, rs.getString(6));
                eq.setActive(rs.getBoolean(7));
                eq.setPromotionLegs(Ranks.RANK_SO, rs.getInt(8));
                eq.setPromotionHours(Ranks.RANK_SO, rs.getInt(9));
                eq.setPromotionLegs(Ranks.RANK_C, rs.getInt(10));
                eq.setPromotionHours(Ranks.RANK_C, rs.getInt(11));
                eq.setCPName(rs.getString(12));
                eq.setCPEmail(rs.getString(13));

                // Add to the results
                results.add(eq);
            }
            
            // The last two columns are the additional rating info
            switch (rs.getInt(14)) {
                case EquipmentType.PRIMARY_RATING :
                    eq.addPrimaryRating(rs.getString(15));
                    break;
                
                default :
                case EquipmentType.SECONDARY_RATING :
                    eq.addSecondaryRating(rs.getString(15));
                    break;
            }
        }

        // Clean up JDBC resources
        rs.close();
        _ps.close();
        return results;
    }
}