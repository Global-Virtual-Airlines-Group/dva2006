// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2013, 2015, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to write Equipment Profiles.
 * @author Luke
 * @version 9.0
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
			
			// Write shared entry
			try (PreparedStatement ps = prepare("INSERT INTO common.EQPROGRAMS (EQTYPE, OWNER, STAGE) VALUES (?, ?, ?)")) {
				ps.setString(1, eq.getName());
				ps.setString(2, SystemData.get("airline.code"));
				ps.setInt(3, eq.getStage());
				executeUpdate(ps, 1);
			}
			
			// Clear old default type
			if (eq.getIsDefault())
				clearDefaultType();
			
			// Write to the local database
			try (PreparedStatement ps = prepare("INSERT INTO EQTYPES (EQTYPE, CP_ID, RANKS, ACTIVE, NEWHIRES, ISDEFAULT, C_LEGS, C_HOURS, C_LEGS_ACARS, C_LEGS_DISTANCE, C_SWITCH_DISTANCE, "
				+ "C_MIN_1X, C_MAX_ACCEL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, eq.getName());
				ps.setInt(2, eq.getCPID());
				ps.setString(3, StringUtils.listConcat(eq.getRanks(), ","));
				ps.setBoolean(4, eq.getActive());
				ps.setBoolean(5, eq.getNewHires());
				ps.setBoolean(6, eq.getIsDefault());
				ps.setInt(7, eq.getPromotionLegs());
				ps.setDouble(8, eq.getPromotionHours());
				ps.setBoolean(9, eq.getACARSPromotionLegs());
				ps.setInt(10, eq.getPromotionMinLength());
				ps.setInt(11, eq.getPromotionSwitchLength());
				ps.setInt(12, eq.getMinimum1XTime());
				ps.setInt(13, eq.getMaximumAccelTime());
				executeUpdate(ps, 1);
			}
				
			// Write the exams/ratings and commit
			writeExams(eq);
			writeRatings(eq);
			writeAirlines(eq);
			commitTransaction();
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
			Object oldCacheKey = eq.cacheKey();
			startTransaction();
			
			// Update the name if necessary in common table
			if (newName != null) {
				try (PreparedStatement ps = prepareWithoutLimits("UPDATE common.EQPROGRAMS SET EQTYPE=?, STAGE=? WHERE (EQTYPE=?) AND (OWNER=?)")) {
					ps.setString(1, newName);
					ps.setInt(2, eq.getStage());
					ps.setString(3, eq.getName());
					ps.setString(4, SystemData.get("airline.code"));
					executeUpdate(ps, 1);
				}
			}
			
			// Clear default program if necessary
			if (eq.getIsDefault())
				clearDefaultType();
			
			// Update the program
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE EQTYPES SET CP_ID=?, RANKS=?, ACTIVE=?, NEWHIRES=?, ISDEFAULT=?, C_LEGS=?, C_HOURS=?, C_LEGS_ACARS=?, C_LEGS_DISTANCE=?, "
				+ "C_SWITCH_DISTANCE=?, C_MIN_1X=?, C_MAX_ACCEL=?, EQTYPE=? WHERE (EQTYPE=?)")) {
				ps.setInt(1, eq.getCPID());
				ps.setString(2, StringUtils.listConcat(eq.getRanks(), ","));
				ps.setBoolean(3, eq.getActive());
				ps.setBoolean(4, eq.getNewHires());
				ps.setBoolean(5, eq.getIsDefault());
				ps.setInt(6, eq.getPromotionLegs());
				ps.setDouble(7, eq.getPromotionHours());
				ps.setBoolean(8, eq.getACARSPromotionLegs());
				ps.setInt(9, eq.getPromotionMinLength());
				ps.setInt(10, eq.getPromotionSwitchLength());
				ps.setInt(11, eq.getMinimum1XTime());
				ps.setInt(12, eq.getMaximumAccelTime());
				ps.setString(13, (newName == null) ? eq.getName() : newName);
				ps.setString(14, eq.getName());
				executeUpdate(ps, 1);
			}
			
			// Update the name if neccessary
			if (newName != null)
				eq.setName(newName);
			
			// Write the exams/ratings and commit
			writeExams(eq);
			writeRatings(eq);
			writeAirlines(eq);
			commitTransaction();
			CacheManager.invalidate("EquipmentTypes", oldCacheKey);
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to write examination names.
	 */
	private void writeExams(EquipmentType eq) throws SQLException {
		
		// Clean out exams
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM EQEXAMS WHERE (EQTYPE=?)")) {
			ps.setString(1, eq.getName());
			executeUpdate(ps, 0);
		}
		
		// Write exams
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO EQEXAMS (EQTYPE, EXAM, EXAMTYPE) VALUES (?, ?, ?)")) {
			ps.setString(1, eq.getName());
		
			// Write the FO exams
			ps.setInt(3, Rank.FO.ordinal());
			for (String examName : eq.getExamNames(Rank.FO)) {
				ps.setString(2, examName);
				ps.addBatch();
			}
		
			// Write the Captain exams
			ps.setInt(3, Rank.C.ordinal());
			for (String examName : eq.getExamNames(Rank.C)) {
				ps.setString(2, examName);
				ps.addBatch();
			}
		
			executeUpdate(ps, 1, 0);
		}
	}
	
	/*
	 * Helper method to clear default equipment type as part of a transaction. 
	 */
	private void clearDefaultType() throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("UPDATE EQTYPES SET ISDEFAULT=? WHERE (ISDEFAULT=?)")) {
			ps.setBoolean(1, false);
			ps.setBoolean(2, true);
			executeUpdate(ps, 0);
		}
	}
	
	/*
	 * Helper method to update primary/secondary ratings.
	 */
	private void writeRatings(EquipmentType eq) throws SQLException {
		
		// Clean out ratings
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM EQRATINGS WHERE (EQTYPE=?)")) {
			ps.setString(1, eq.getName());
			executeUpdate(ps, 0);
		}
		
		// Write the ratings
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO EQRATINGS (EQTYPE, RATING_TYPE, RATED_EQ) VALUES (?, ?, ?)")) {
			ps.setString(1, eq.getName());
		
			// Add the primary ratings
			ps.setInt(2, EquipmentType.Rating.PRIMARY.ordinal());
			for (String rating : eq.getPrimaryRatings()) {
				ps.setString(3, rating);
				ps.addBatch();
			}
		
			// Add the secondary ratings
			ps.setInt(2, EquipmentType.Rating.SECONDARY.ordinal());
			for (String rating : eq.getSecondaryRatings()) {
				ps.setString(3, rating);
				ps.addBatch();
			}
		
			executeUpdate(ps, 1, 0);
		}
	}

	/*
	 * Helper method to write airline records for an equipment profile.
	 */
	private void writeAirlines(EquipmentType eq) throws SQLException {
		
		// Clean out airlines
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM common.EQAIRLINES WHERE (EQTYPE=?) AND (OWNER=?)")) {
			ps.setString(1, eq.getName());
			ps.setString(2, SystemData.get("airline.code"));
			executeUpdate(ps, 0);
		}
		
		// Add the airlines
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO common.EQAIRLINES (EQTYPE, OWNER, AIRLINE) VALUES (?, ?, ?)")) {
			ps.setString(1, eq.getName());
			ps.setString(2, SystemData.get("airline.code"));
			for (AirlineInformation ai : eq.getAirlines()) {
				ps.setString(3, ai.getCode());
				ps.addBatch();
			}
		
			executeUpdate(ps, 1, eq.getAirlines().size());
		}
	}
}