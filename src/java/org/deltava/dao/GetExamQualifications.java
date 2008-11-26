// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.testing.Test;

import org.deltava.util.CollectionUtils;

/**
 * A Data Access Object to load Pilot IDs for Pilots who meet the entrance
 * qualifications for an Equipment Type program.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class GetExamQualifications extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetExamQualifications(Connection c) {
		super(c);
	}

	/**
	 * Returns the database IDs for all Pilots who meet the entrance criteria for this
	 * equipment type program.
	 * @param eq the EquipmentType bean
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getRatedPilots(EquipmentType eq) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT DISTINCT PILOT_ID FROM exams.CHECKRIDES "
					+ "WHERE (STATUS=?) AND (PASS=?) AND (EQTYPE=?)");
			_ps.setInt(1, Test.SCORED);
			_ps.setBoolean(2, true);
			_ps.setString(3, eq.getName());
			
			// Load checkrides
			Collection<Integer> crIDs = new HashSet<Integer>();
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				crIDs.add(new Integer(rs.getInt(1)));
			
			// Clean up
			rs.close();
			_ps.close();
			
			// Build the SQL statement
			Collection<String> examNames = eq.getExamNames(Ranks.RANK_FO);
			StringBuilder buf = new StringBuilder("SELECT PILOT_ID, COUNT(ID) AS CNT FROM exams.EXAMS "
					+ "WHERE (STATUS=?) AND (PASS=?) AND (");
			for (Iterator<String> i = examNames.iterator(); i.hasNext(); ) {
				i.next();
				buf.append("(NAME=?)");
				if (i.hasNext())
					buf.append(" OR ");
			}
			
			buf.append(") GROUP BY PILOT_ID HAVING (CNT>=?)");
			
			// Init the SQL statement
			int pos = 0;
			prepareStatementWithoutLimits(buf.toString());
			_ps.setInt(++pos, Test.SCORED);
			_ps.setBoolean(++pos, true);
			for (Iterator<String> i = examNames.iterator(); i.hasNext(); )
				_ps.setString(++pos, i.next());
			_ps.setInt(++pos, examNames.size());
			
			// Load exams
			Collection<Integer> examIDs = new HashSet<Integer>();
			rs = _ps.executeQuery();
			while (rs.next())
				examIDs.add(new Integer(rs.getInt(1)));
			
			// Clean up
			rs.close();
			_ps.close();
			
			// Return the union of the two
			return CollectionUtils.union(examIDs, crIDs);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}