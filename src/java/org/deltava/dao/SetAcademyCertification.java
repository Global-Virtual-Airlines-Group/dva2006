// Copyright 2006, 2007, 2008, 2010, 2011, 2014, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object for Flight Academy Certifications and Check Ride Scripts.
 * @author Luke
 * @version 7.2
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
			prepareStatementWithoutLimits("INSERT INTO exams.CERTS (NAME, ABBR, STAGE, PREREQ, ACTIVE, AUTO_ENROLL, VISIBLE, CHECKRIDES, EQPROGRAM, FLIGHTCOUNT, DESCRIPTION) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			_ps.setString(1, c.getName());
			_ps.setString(2, c.getCode());
			_ps.setInt(3, c.getStage());
			_ps.setInt(4, c.getReqs());
			_ps.setBoolean(5, c.getActive());
			_ps.setBoolean(6, c.getAutoEnroll());
			_ps.setBoolean(7, c.getVisible());
			_ps.setInt(8, c.getRideCount());
			_ps.setString(9, c.getEquipmentProgram());
			_ps.setInt(10, c.getFlightCount());
			_ps.setString(11, c.getDescription());
			executeUpdate(1);
			
			// If we've got a pre-req, write it
			if (c.getReqs() == Certification.REQ_SPECIFIC)
				writePrereq(c.getName(), c.getReqCert());
			
			// Write the exams/roles
			writeExams(c.getName(), c.getExamNames());
			writeRoles(c.getName(), c.getRoles());
			writeApps(c.getName(), c.getAirlines());
			writeEQ(c.getName(), c.getRideEQ());
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
			prepareStatementWithoutLimits("UPDATE exams.CERTS SET NAME=?, ABBR=?, STAGE=?, PREREQ=?, ACTIVE=?, AUTO_ENROLL=?, VISIBLE=?, CHECKRIDES=?, EQPROGRAM=?, "
				+ "FLIGHTCOUNT=?, DESCRIPTION=? WHERE (NAME=?)");
			_ps.setString(1, c.getName());
			_ps.setString(2, c.getCode());
			_ps.setInt(3, c.getStage());
			_ps.setInt(4, c.getReqs());
			_ps.setBoolean(5, c.getActive());
			_ps.setBoolean(6, c.getAutoEnroll());
			_ps.setBoolean(7, c.getVisible());
			_ps.setInt(8, c.getRideCount());
			_ps.setString(9, c.getEquipmentProgram());
			_ps.setInt(10, c.getFlightCount());
			_ps.setString(11, c.getDescription());
			_ps.setString(12, name);
			executeUpdate(1);
			
			// Write the pre-requisite
			writePrereq(c.getName(), c.getReqCert());
			
			// Clear the exams
			prepareStatementWithoutLimits("DELETE FROM exams.CERTEXAMS WHERE (CERTNAME=?)");
			_ps.setString(1, c.getName());
			executeUpdate(0);
			
			// Clear the roles
			prepareStatementWithoutLimits("DELETE FROM exams.CERTROLES WHERE (CERTNAME=?)");
			_ps.setString(1, c.getName());
			executeUpdate(0);
			
			// Clear the requirements
			prepareStatementWithoutLimits("DELETE FROM exams.CERTREQS WHERE (CERTNAME=?)");
			_ps.setString(1, c.getName());
			executeUpdate(0);
			
			// Clear the Apps
			prepareStatementWithoutLimits("DELETE FROM exams.CERTAPPS WHERE (CERTNAME=?)");
			_ps.setString(1, c.getName());
			executeUpdate(0);
			
			// Clear the check ride EQ
			prepareStatementWithoutLimits("DELETE FROM exams.CERTEQ WHERE (CERTNAME=?)");
			_ps.setString(1, c.getName());
			executeUpdate(0);
			
			// Write the exams/roles
			writeExams(c.getName(), c.getExamNames());
			writeRoles(c.getName(), c.getRoles());
			writeApps(c.getName(), c.getAirlines());
			writeEQ(c.getName(), c.getRideEQ());
			
			// Write the requirements
			prepareStatementWithoutLimits("INSERT INTO exams.CERTREQS (CERTNAME, SEQ, EXAMNAME, REQENTRY) VALUES (?, ?, ?, ?)");
			_ps.setString(1, c.getName());
			for (CertificationRequirement req : c.getRequirements()) {
				_ps.setInt(2, req.getID());
				_ps.setString(3, req.getExamName());
				_ps.setString(4, req.getText());
				_ps.addBatch();
			}
			
			_ps.executeBatch();
			_ps.close();
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
			prepareStatement("REPLACE INTO exams.CERTRIDE_SCRIPTS (CERTNAME, IDX, BODY) VALUES (?, ?, ?)");
			_ps.setString(1, sc.getCertificationName());
			_ps.setInt(2, sc.getIndex());
			_ps.setString(3, sc.getDescription());
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
	 * @param id the AcademyRideID
	 * @throws DAOException if a JDBC error occurs
	 */
	public void deleteScript(AcademyRideID id) throws DAOException {
		try {
			prepareStatement("DELETE FROM exams.CERTRIDE_SCRIPTS WHERE (CERTNAME=?) AND (IDX=?)");
			_ps.setString(1, id.getName());
			_ps.setInt(2, id.getIndex());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to write specific Certification pre-requsite.
	 */
	private void writePrereq(String certName, String prereqAbbr) throws SQLException {
		if (!StringUtils.isEmpty(prereqAbbr)) {
			prepareStatementWithoutLimits("REPLACE INTO exams.CERTPREREQ (NAME, ABBR) VALUES (?, ?)");
			_ps.setString(1, certName);
			_ps.setString(2, prereqAbbr);
			executeUpdate(1);
		} else {
			prepareStatementWithoutLimits("DELETE FROM exams.CERTPREREQ WHERE (NAME=?)");
			_ps.setString(1, certName);
			executeUpdate(0);
		}
	}
	
	/*
	 * Helper method to write security role names.
	 */
	private void writeRoles(String certName, Collection<String> roles) throws SQLException {
		prepareStatementWithoutLimits("INSERT INTO exams.CERTROLES (CERTNAME, ROLE) VALUES (?, ?)");
		_ps.setString(1, certName);
		for (String role : roles) {
			_ps.setString(2, role);
			_ps.addBatch();			
		}
		
		_ps.executeBatch();
		_ps.close();
	}
	
	/*
	 * Helper method to write exam names.
	 */
	private void writeExams(String certName, Collection<String> exams) throws SQLException {
		prepareStatementWithoutLimits("INSERT INTO exams.CERTEXAMS (CERTNAME, EXAMNAME) VALUES (?, ?)");
		_ps.setString(1, certName);
		for (String exam : exams) {
			_ps.setString(2, exam);
			_ps.addBatch();
		}
		
		_ps.executeBatch();
		_ps.close();
	}
	
	/*
	 * Helper method to write virtual ailrine names.
	 */
	private void writeApps(String certName, Collection<AirlineInformation> airlines) throws SQLException {
		prepareStatementWithoutLimits("INSERT INTO exams.CERTAPPS (CERTNAME, AIRLINE) VALUES (?, ?)");
		_ps.setString(1, certName);
		for (AirlineInformation ai : airlines) {
			_ps.setString(2, ai.getCode());
			_ps.addBatch();
		}
		
		_ps.executeBatch();
		_ps.close();
	}
	
	/*
	 * Helper method to write check ride equipment types.
	 */
	private void writeEQ(String certName, Collection<String> eqTypes) throws SQLException {
		prepareStatementWithoutLimits("INSERT INTO exams.CERTEQ (CERTNAME, EQTYPE) VALUES (?, ?)");
		_ps.setString(1, certName);
		for (String eqType : eqTypes) {
			_ps.setString(2, eqType);
			_ps.addBatch();
		}
		
		_ps.executeBatch();
		_ps.close();
	}
}