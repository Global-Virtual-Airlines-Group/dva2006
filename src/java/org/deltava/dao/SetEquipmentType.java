// Copyright 2005, 2006, 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Iterator;

import org.deltava.beans.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write Equipment Profiles.
 * @author Luke
 * @version 3.3
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
			
			// Write shared entry
			prepareStatement("INSERT INTO common.EQPROGRAMS (EQTYPE, OWNER, STAGE) VALUES (?, ?, ?)");
			_ps.setString(1, eq.getName());
			_ps.setString(2, SystemData.get("airline.code"));
			_ps.setInt(3, eq.getStage());
			executeUpdate(1);
			
			// Write to the local database
			prepareStatement("INSERT INTO EQTYPES (EQTYPE, CP_ID, RANKS, ACTIVE, C_LEGS, C_HOURS, "
				+ "C_LEGS_ACARS, C_LEGS_DISTANCE, C_SWITCH_DISTANCE, C_MIN_1X, C_MAX_ACCEL) VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, eq.getName());
			_ps.setInt(2, eq.getCPID());
			_ps.setString(3, StringUtils.listConcat(eq.getRanks(), ","));
			_ps.setBoolean(4, eq.getActive());
			_ps.setInt(5, eq.getPromotionLegs());
			_ps.setDouble(6, eq.getPromotionHours());
			_ps.setBoolean(7, eq.getACARSPromotionLegs());
			_ps.setInt(8, eq.getPromotionMinLength());
			_ps.setInt(9, eq.getPromotionSwitchLength());
			_ps.setInt(10, eq.getMinimum1XTime());
			_ps.setInt(11, eq.getMaximumAccelTime());
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
			String oldName = eq.getName();
			startTransaction();
			
			// Update the name if necessary in common table
			if (newName != null) {
				prepareStatementWithoutLimits("UPDATE common.EQPROGRAMS SET EQTYPE=?, STAGE=? WHERE (EQTYPE=?) AND (OWNER=?)");
				_ps.setString(1, newName);
				_ps.setInt(2, eq.getStage());
				_ps.setString(3, eq.getName());
				_ps.setString(4, SystemData.get("airline.code"));
				executeUpdate(1);
			}
			
			prepareStatementWithoutLimits("UPDATE EQTYPES SET CP_ID=?, RANKS=?, ACTIVE=?, C_LEGS=?, C_HOURS=?, "
					+ "C_LEGS_ACARS=?, C_LEGS_DISTANCE=?, C_SWITCH_DISTANCE=?, C_MIN_1X=?, C_MAX_ACCEL=?, EQTYPE=? "
					+ "WHERE (EQTYPE=?)");
			_ps.setInt(1, eq.getCPID());
			_ps.setString(2, StringUtils.listConcat(eq.getRanks(), ","));
			_ps.setBoolean(3, eq.getActive());
			_ps.setInt(4, eq.getPromotionLegs());
			_ps.setDouble(5, eq.getPromotionHours());
			_ps.setBoolean(6, eq.getACARSPromotionLegs());
			_ps.setInt(7, eq.getPromotionMinLength());
			_ps.setInt(8, eq.getPromotionSwitchLength());
			_ps.setInt(9, eq.getMinimum1XTime());
			_ps.setInt(10, eq.getMaximumAccelTime());
			_ps.setString(11, (newName == null) ? eq.getName() : newName);
			_ps.setString(12, eq.getName());
			executeUpdate(1);
			
			// Update the name if neccessary
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
		for (Iterator<String> i = eq.getExamNames(Rank.FO).iterator(); i.hasNext(); ) {
			_ps.setString(2, i.next());
			_ps.addBatch();
		}
		
		// Write the Captain exams
		_ps.setInt(3, EquipmentType.EXAM_CAPT);
		for (Iterator<String> i = eq.getExamNames(Rank.C).iterator(); i.hasNext(); ) {
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
		prepareStatementWithoutLimits("DELETE FROM common.EQAIRLINES WHERE (EQTYPE=?) AND (OWNER=?)");
		_ps.setString(1, eq.getName());
		_ps.setString(2, SystemData.get("airline.code"));
		executeUpdate(0);
		
		// Prepare the statement and add the airlines
		prepareStatementWithoutLimits("INSERT INTO common.EQAIRLINES (EQTYPE, OWNER, AIRLINE) VALUES (?, ?, ?)");
		_ps.setString(1, eq.getName());
		_ps.setString(2, SystemData.get("airline.code"));
		for (Iterator<AirlineInformation> i = eq.getAirlines().iterator(); i.hasNext(); ) {
			AirlineInformation ai = i.next();
			_ps.setString(3, ai.getCode());
			_ps.addBatch();
		}
		
		// Execute the batch update
		_ps.executeBatch();
		_ps.close();
	}
}