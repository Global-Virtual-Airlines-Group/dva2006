// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
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
			prepareStatement("INSERT INTO EQTYPES (EQTYPE, CP_ID, STAGE, RANKS, EXAM_FO, EXAM_CAPT, ACTIVE, " +
					"SO_LEGS, SO_HOURS, C_LEGS, C_HOURS, C_LEGS_ACARS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			_ps.setString(1, eq.getName());
			_ps.setInt(2, eq.getCPID());
			_ps.setInt(3, eq.getStage());
			_ps.setString(4, StringUtils.listConcat(eq.getRanks(), ","));
			_ps.setString(5, eq.getExamName(Ranks.RANK_FO));
			_ps.setString(6, eq.getExamName(Ranks.RANK_C));
			_ps.setBoolean(7, eq.getActive());
			_ps.setInt(8, eq.getPromotionLegs(Ranks.RANK_SO));
			_ps.setDouble(9, eq.getPromotionHours(Ranks.RANK_SO));
			_ps.setInt(10, eq.getPromotionLegs(Ranks.RANK_C));
			_ps.setDouble(11, eq.getPromotionHours(Ranks.RANK_C));
			_ps.setBoolean(12, eq.getACARSPromotionLegs());
			
			// Update the equipment profile
			executeUpdate(1);
			
			// Write the ratings and commit
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
			prepareStatement("UPDATE EQTYPES SET CP_ID=?, STAGE=?, RANKS=?, EXAM_FO=?, EXAM_CAPT=?, ACTIVE=?, " +
					"SO_LEGS=?, SO_HOURS=?, C_LEGS=?, C_HOURS=?, C_LEGS_ACARS=? WHERE (EQTYPE=?)");
			
			_ps.setInt(1, eq.getCPID());
			_ps.setInt(2, eq.getStage());
			_ps.setString(3, StringUtils.listConcat(eq.getRanks(), ","));
			_ps.setString(4, eq.getExamName(Ranks.RANK_FO));
			_ps.setString(5, eq.getExamName(Ranks.RANK_C));
			_ps.setBoolean(6, eq.getActive());
			_ps.setInt(7, eq.getPromotionLegs(Ranks.RANK_SO));
			_ps.setDouble(8, eq.getPromotionHours(Ranks.RANK_SO));
			_ps.setInt(9, eq.getPromotionLegs(Ranks.RANK_C));
			_ps.setDouble(10, eq.getPromotionHours(Ranks.RANK_C));
			_ps.setBoolean(11, eq.getACARSPromotionLegs());
			_ps.setString(12, eq.getName());
			
			// Update the equipment profile
			executeUpdate(1);
			
			// Write the ratings and commit
			writeRatings(eq);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	public void rename(EquipmentType eq, String oldName) throws DAOException {

		try {
			startTransaction();
			prepareStatement("UPDATE EQTYPES SET EQTYPE=? WHERE (EQTYPE=?)");
			_ps.setString(1, eq.getName());
			_ps.setString(2, oldName);
			
			// Update the equipment profile
			executeUpdate(1);
			
			// Rename the pilots
			prepareStatementWithoutLimits("UPDATE PILOTS SET EQTYPE=? WHERE (EQTYPE=?)");
			_ps.setString(1, eq.getName());
			_ps.setString(2, oldName);

			// Update the pilot profiles and commit
			executeUpdate(0);
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
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