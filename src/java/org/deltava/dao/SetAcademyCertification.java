// Copyright 2006, 2007, 2008, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object for Flight Academy Certifications and Check Ride Scripts.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class SetAcademyCertification extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetAcademyCertification(Connection c) {
		super(c);
	}

	/**
	 * Writes a new Certification to the database.
	 * @param c the Certification bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Certification c) throws DAOException {
		try {
			startTransaction();
			
			// Write the certification entry
			prepareStatementWithoutLimits("INSERT INTO exams.CERTS (NAME, ABBR, STAGE, PREREQ, REQCERT, ACTIVE, "
				+ "AUTO_ENROLL, HAS_CHECKRIDE, DESCRIPTION) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, c.getName());
			_ps.setString(2, c.getCode());
			_ps.setInt(3, c.getStage());
			_ps.setInt(4, c.getReqs());
			_ps.setString(5, c.getReqCert());
			_ps.setBoolean(6, c.getActive());
			_ps.setBoolean(7, c.getAutoEnroll());
			_ps.setBoolean(8, c.getHasCheckRide());
			_ps.setString(9, c.getDescription());
			executeUpdate(1);
			
			// Write the exams
			writeExams(c.getName(), c.getExamNames());
			
			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Updates an existing Flight Academy Certification profile.
	 * @param c the Certification bean
	 * @param name the Certification name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void update(Certification c, String name) throws DAOException {
		try {
			startTransaction();
			
			// Write the profile
			prepareStatementWithoutLimits("UPDATE exams.CERTS SET NAME=?, ABBR=?, STAGE=?, PREREQ=?, REQCERT=?, "
				+ "ACTIVE=?, AUTO_ENROLL=?, HAS_CHECKRIDE=?, DESCRIPTION=? WHERE (NAME=?)");
			_ps.setString(1, c.getName());
			_ps.setString(2, c.getCode());
			_ps.setInt(3, c.getStage());
			_ps.setInt(4, c.getReqs());
			_ps.setString(5, c.getReqCert());
			_ps.setBoolean(6, c.getActive());
			_ps.setBoolean(7, c.getAutoEnroll());
			_ps.setBoolean(8, c.getHasCheckRide());
			_ps.setString(9, c.getDescription());
			_ps.setString(10, name);
			executeUpdate(1);
			
			// Clear the exams
			prepareStatementWithoutLimits("DELETE FROM exams.CERTEXAMS WHERE (CERTNAME=?)");
			_ps.setString(1, c.getName());
			executeUpdate(0);
			
			// Clear the requirements
			prepareStatementWithoutLimits("DELETE FROM exams.CERTREQS WHERE (CERTNAME=?)");
			_ps.setString(1, c.getName());
			executeUpdate(0);
			
			// Write the exams
			writeExams(c.getName(), c.getExamNames());
			
			// Write the requirements
			prepareStatementWithoutLimits("INSERT INTO exams.CERTREQS (CERTNAME, SEQ, EXAMNAME, REQENTRY) VALUES (?, ?, ?, ?)");
			_ps.setString(1, c.getName());
			for (Iterator<CertificationRequirement> i = c.getRequirements().iterator(); i.hasNext(); ) {
				CertificationRequirement req = i.next();
				_ps.setInt(2, req.getID());
				_ps.setString(3, req.getExamName());
				_ps.setString(4, req.getText());
				_ps.addBatch();
			}
			
			// Execute the batch transaction
			_ps.executeBatch();
			_ps.close();

			// Commit the transaction
			commitTransaction();
		} catch (SQLException se) {
			rollbackTransaction();
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes a Flight Academy Check Ride script to the database.
	 * @param sc the AcademyRideScript bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(AcademyRideScript sc) throws DAOException {
		try {
			prepareStatement("REPLACE INTO exams.CERTRIDE_SCRIPTS (CERTNAME, BODY) VALUES (?, ?)");
			_ps.setString(1, sc.getCertificationName());
			_ps.setString(2, sc.getDescription());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Flight Academy Certification from the database.
	 * @param certName the certification name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(String certName) throws DAOException {
		try {
			prepareStatement("DELETE FROM exams.CERTS WHERE (NAME=?)");
			_ps.setString(1, certName);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Deletes a Flight Academy Check Ride script from the database.
	 * @param certName the certification name
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteScript(String certName) throws DAOException {
		try {
			prepareStatement("DELETE FROM exams.CERTRIDE_SCRIPTS WHERE (CERTNAME=?)");
			_ps.setString(1, certName);
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to write exam names.
	 */
	private void writeExams(String certName, Collection<String> exams) throws SQLException {

		// Prepare the statement
		prepareStatementWithoutLimits("INSERT INTO exams.CERTEXAMS (CERTNAME, EXAMNAME) VALUES (?, ?)");
		_ps.setString(1, certName);
		for (Iterator<String> i = exams.iterator(); i.hasNext(); ) {
			_ps.setString(2, i.next());
			_ps.addBatch();
		}
		
		// Execute the batch transaction
		_ps.executeBatch();
		_ps.close();
		_ps = null;
	}
}