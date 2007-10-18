// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.EquipmentType;
import org.deltava.beans.Ranks;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to write Equipment Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SetEquipmentType extends DAO {

	/**
	 * Initialze the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetEquipmentType(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Equipment Type profile to the database.
	 * @param eq the EquipmentType bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void create(EquipmentType eq) throws DAOException {
		try {
			startTransaction();
			prepareStatement("INSERT INTO EQTYPES (EQTYPE, CP_ID, STAGE, RANKS, ACTIVE, SO_LEGS, SO_HOURS, "
					+ "C_LEGS, C_HOURS, C_LEGS_ACARS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			_ps.setString(1, eq.getName());
			_ps.setInt(2, eq.getCPID());
			_ps.setInt(3, eq.getStage());
			_ps.setString(4, StringUtils.listConcat(eq.getRanks(), ","));
			_ps.setBoolean(5, eq.getActive());
			_ps.setInt(6, eq.getPromotionLegs(Ranks.RANK_SO));
			_ps.setDouble(7, eq.getPromotionHours(Ranks.RANK_SO));
			_ps.setInt(8, eq.getPromotionLegs(Ranks.RANK_C));
			_ps.setDouble(9, eq.getPromotionHours(Ranks.RANK_C));
			_ps.setBoolean(10, eq.getACARSPromotionLegs());
			executeUpdate(1);
			
			// Write the exams/ratings and commit
			writeExams(eq);
			writeRatings(eq);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Equipment Type profile in the database.
	 * @param eq the EquipmentType bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(EquipmentType eq) throws DAOException {
		try {
			startTransaction();
			prepareStatement("UPDATE EQTYPES SET CP_ID=?, STAGE=?, RANKS=?, ACTIVE=?, SO_LEGS=?, SO_HOURS=?, "
					+ "C_LEGS=?, C_HOURS=?, C_LEGS_ACARS=? WHERE (EQTYPE=?)");
			
			_ps.setInt(1, eq.getCPID());
			_ps.setInt(2, eq.getStage());
			_ps.setString(3, StringUtils.listConcat(eq.getRanks(), ","));
			_ps.setBoolean(4, eq.getActive());
			_ps.setInt(5, eq.getPromotionLegs(Ranks.RANK_SO));
			_ps.setDouble(6, eq.getPromotionHours(Ranks.RANK_SO));
			_ps.setInt(7, eq.getPromotionLegs(Ranks.RANK_C));
			_ps.setDouble(8, eq.getPromotionHours(Ranks.RANK_C));
			_ps.setBoolean(9, eq.getACARSPromotionLegs());
			_ps.setString(10, eq.getName());
			executeUpdate(1);
			
			// Write the exams/ratings and commit
			writeExams(eq);
			writeRatings(eq);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	private void writeExams(EquipmentType eq) throws SQLException {
		
		// Clean out exams
		prepareStatementWithoutLimits("DELETE FROM EQEXAMS WHERE (EQTYPE=?)");
		_ps.setString(1, eq.getName());
		executeUpdate(0);
		
		// Write exams
		prepareStatementWithoutLimits("INSERT INTO EQEXAMS (EQTYPE, EXAM, EXAMTYPE) VALUES (?, ?, ?)");
		_ps.setString(1, eq.getName());
		
		// Write the FO exams
		_ps.setInt(3, EquipmentType.EXAM_FO);
		for (Iterator<String> i = eq.getExamNames(Ranks.RANK_FO).iterator(); i.hasNext(); ) {
			_ps.setString(2, i.next());
			_ps.addBatch();
		}
		
		// Write the Captain exams
		_ps.setInt(3, EquipmentType.EXAM_CAPT);
		for (Iterator<String> i = eq.getExamNames(Ranks.RANK_C).iterator(); i.hasNext(); ) {
			_ps.setString(2, i.next());
			_ps.addBatch();
		}
		
		// Execute the batch update
		_ps.executeBatch();
		_ps.close();
	}
	
	/**
	 * Helper method to update primary/secondary ratings.
	 */
	private void writeRatings(EquipmentType eq) throws SQLException {
		
		// Clean out ratings
		prepareStatementWithoutLimits("DELETE FROM EQRATINGS WHERE (EQTYPE=?)");
		_ps.setString(1, eq.getName());
		executeUpdate(0);
		
		// Prepare the statement
		prepareStatementWithoutLimits("INSERT INTO EQRATINGS (EQTYPE, RATING_TYPE, RATED_EQ) VALUES (?, ?, ?)");
		_ps.setString(1, eq.getName());
		
		// Add the primary ratings
		_ps.setInt(2, EquipmentType.PRIMARY_RATING);
		for (Iterator i = eq.getPrimaryRatings().iterator(); i.hasNext(); ) {
			_ps.setString(3, (String) i.next());
			_ps.addBatch();
		}
		
		// Add the secondary ratings
		_ps.setInt(2, EquipmentType.SECONDARY_RATING);
		for (Iterator i = eq.getSecondaryRatings().iterator(); i.hasNext(); ) {
			_ps.setString(3, (String) i.next());
			_ps.addBatch();
		}
		
		// Execute the batch update
		_ps.executeBatch();
		_ps.close();
	}
}