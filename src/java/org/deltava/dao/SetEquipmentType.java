// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to write Equipment Profiles.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class SetEquipmentType extends EquipmentTypeDAO {

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
			prepareStatement("INSERT INTO EQTYPES (EQTYPE, CP_ID, STAGE, RANKS, ACTIVE, C_LEGS, C_HOURS, "
				+ "C_LEGS_ACARS, C_LEGS_DISTANCE, C_SWITCH_DISTANCE, C_MIN_1X, C_MAX_ACCEL) VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			_ps.setString(1, eq.getName());
			_ps.setInt(2, eq.getCPID());
			_ps.setInt(3, eq.getStage());
			_ps.setString(4, StringUtils.listConcat(eq.getRanks(), ","));
			_ps.setBoolean(5, eq.getActive());
			_ps.setInt(7, eq.getPromotionLegs());
			_ps.setDouble(8, eq.getPromotionHours());
			_ps.setBoolean(9, eq.getACARSPromotionLegs());
			_ps.setInt(10, eq.getPromotionMinLength());
			_ps.setInt(11, eq.getPromotionSwitchLength());
			_ps.setInt(12, eq.getMinimum1XTime());
			_ps.setInt(13, eq.getMaximumAccelTime());
			executeUpdate(1);
			
			// Write the exams/ratings and commit
			writeExams(eq);
			writeRatings(eq);
			writeAirlines(eq);
			commitTransaction();
			_cache.add(eq);
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}

	/**
	 * Updates an existing Equipment Type profile in the database.
	 * @param eq the EquipmentType bean
	 * @param newName the new equipment type name, or null if the same
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(EquipmentType eq, String newName) throws DAOException {
		try {
			startTransaction();
			prepareStatementWithoutLimits("UPDATE EQTYPES SET CP_ID=?, STAGE=?, RANKS=?, ACTIVE=?, C_LEGS=?, C_HOURS=?, "
					+ "C_LEGS_ACARS=?, C_LEGS_DISTANCE=?, C_SWITCH_DISTANCE=?, C_MIN_1X=?, C_MAX_ACCEL=?, EQTYPE=? "
					+ "WHERE (EQTYPE=?) LIMIT 1");
			
			_ps.setInt(1, eq.getCPID());
			_ps.setInt(2, eq.getStage());
			_ps.setString(3, StringUtils.listConcat(eq.getRanks(), ","));
			_ps.setBoolean(4, eq.getActive());
			_ps.setInt(5, eq.getPromotionLegs());
			_ps.setDouble(6, eq.getPromotionHours());
			_ps.setBoolean(7, eq.getACARSPromotionLegs());
			_ps.setInt(8, eq.getPromotionMinLength());
			_ps.setInt(9, eq.getPromotionSwitchLength());
			_ps.setInt(10, eq.getMinimum1XTime());
			_ps.setInt(11, eq.getMaximumAccelTime());
			_ps.setString(12, (newName == null) ? eq.getName() : newName);
			_ps.setString(13, eq.getName());
			executeUpdate(1);
			
			// Update the name if neccessary
			String oldName = eq.getName();
			if (newName != null)
				eq.setName(newName);
			
			// Write the exams/ratings and commit
			writeExams(eq);
			writeRatings(eq);
			writeAirlines(eq);
			invalidate(oldName);
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
		for (Iterator<String> i = eq.getPrimaryRatings().iterator(); i.hasNext(); ) {
			_ps.setString(3, i.next());
			_ps.addBatch();
		}
		
		// Add the secondary ratings
		_ps.setInt(2, EquipmentType.SECONDARY_RATING);
		for (Iterator<String> i = eq.getSecondaryRatings().iterator(); i.hasNext(); ) {
			_ps.setString(3, i.next());
			_ps.addBatch();
		}
		
		// Execute the batch update
		_ps.executeBatch();
		_ps.close();
	}

	/**
	 * Helper method to write airline records for an equipment profile.
	 */
	private void writeAirlines(EquipmentType eq) throws SQLException {
		
		// Clean out airlines
		prepareStatementWithoutLimits("DELETE FROM EQAIRLINES WHERE (EQTYPE=?)");
		_ps.setString(1, eq.getName());
		executeUpdate(0);
		
		// Prepare the statement and add the airlines
		prepareStatementWithoutLimits("INSERT INTO EQAIRLINES (EQTYPE, AIRLINE) VALUES (?, ?)");
		_ps.setString(1, eq.getName());
		for (Iterator<AirlineInformation> i = eq.getAirlines().iterator(); i.hasNext(); ) {
			AirlineInformation ai = i.next();
			_ps.setString(2, ai.getCode());
			_ps.addBatch();
		}
		
		// Execute the batch update
		_ps.executeBatch();
		_ps.close();
	}
}