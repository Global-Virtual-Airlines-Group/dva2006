// Copyright 2006, 2007, 2008, 2010, 2011, 2014, 2015, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object for Flight Academy Certifications and Check Ride Scripts.
 * @author Luke
 * @version 9.0
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
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.CERTS (NAME, ABBR, STAGE, PREREQ, ACTIVE, AUTO_ENROLL, VISIBLE, CHECKRIDES, EQPROGRAM, FLIGHTCOUNT, NETWORK, "
				+ "NETWORK, RATINGCODE, DESCRIPTION) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				ps.setString(1, c.getName());
				ps.setString(2, c.getCode());
				ps.setInt(3, c.getStage());
				ps.setInt(4, c.getReqs());
				ps.setBoolean(5, c.getActive());
				ps.setBoolean(6, c.getAutoEnroll());
				ps.setBoolean(7, c.getVisible());
				ps.setInt(8, c.getRideCount());
				ps.setString(9, c.getEquipmentProgram());
				ps.setInt(10, c.getFlightCount());
				ps.setString(11, (c.getNetwork() == null) ? null : c.getNetwork().name());
				ps.setString(12, c.getNetworkRatingCode());
				ps.setString(13, c.getDescription());
				executeUpdate(ps, 1);
			}
			
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
			try (PreparedStatement ps = prepareWithoutLimits("UPDATE exams.CERTS SET NAME=?, ABBR=?, STAGE=?, PREREQ=?, ACTIVE=?, AUTO_ENROLL=?, VISIBLE=?, CHECKRIDES=?, EQPROGRAM=?, FLIGHTCOUNT=?, NETWORK=?, RATINGCODE=?, DESCRIPTION=? WHERE (NAME=?)")) {
				ps.setString(1, c.getName());
				ps.setString(2, c.getCode());
				ps.setInt(3, c.getStage());
				ps.setInt(4, c.getReqs());
				ps.setBoolean(5, c.getActive());
				ps.setBoolean(6, c.getAutoEnroll());
				ps.setBoolean(7, c.getVisible());
				ps.setInt(8, c.getRideCount());
				ps.setString(9, c.getEquipmentProgram());
				ps.setInt(10, c.getFlightCount());
				ps.setString(11, (c.getNetwork() == null) ? null : c.getNetwork().name());
				ps.setString(12, c.getNetworkRatingCode());
				ps.setString(13, c.getDescription());
				ps.setString(14, name);
				executeUpdate(ps, 1);
			}
			
			// Write the pre-requisite
			writePrereq(c.getName(), c.getReqCert());
			
			// Clear the exams
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.CERTEXAMS WHERE (CERTNAME=?)")) {
				ps.setString(1, c.getName());
				executeUpdate(ps, 0);
			}
			
			// Clear the roles
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.CERTROLES WHERE (CERTNAME=?)")) {
				ps.setString(1, c.getName());
				executeUpdate(ps, 0);
			}
			
			// Clear the requirements
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.CERTREQS WHERE (CERTNAME=?)")) {
				ps.setString(1, c.getName());
				executeUpdate(ps, 0);
			}
			
			// Clear the Apps
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.CERTAPPS WHERE (CERTNAME=?)")) {
				ps.setString(1, c.getName());
				executeUpdate(ps, 0);
			}
			
			// Clear the check ride EQ
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.CERTEQ WHERE (CERTNAME=?)")) {
				ps.setString(1, c.getName());
				executeUpdate(ps, 0);
			}
			
			// Write the exams/roles
			writeExams(c.getName(), c.getExamNames());
			writeRoles(c.getName(), c.getRoles());
			writeApps(c.getName(), c.getAirlines());
			writeEQ(c.getName(), c.getRideEQ());
			
			// Write the requirements
			try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.CERTREQS (CERTNAME, SEQ, EXAMNAME, REQENTRY) VALUES (?, ?, ?, ?)")) {
				ps.setString(1, c.getName());
				for (CertificationRequirement req : c.getRequirements()) {
					ps.setInt(2, req.getID());
					ps.setString(3, req.getExamName());
					ps.setString(4, req.getText());
					ps.addBatch();
				}
			
				executeUpdate(ps, 1, c.getRequirements().size());
			}
			
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
		try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO exams.CERTRIDE_SCRIPTS (CERTNAME, IDX, SIMS, BODY) VALUES (?, ?, ?, ?)")) {
			ps.setString(1, sc.getCertificationName());
			ps.setInt(2, sc.getIndex());
			ps.setString(3, StringUtils.listConcat(sc.getSimulators(), ","));
			ps.setString(4, sc.getDescription());
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("DELETE FROM exams.CERTS WHERE (NAME=?)")) {
			ps.setString(1, certName);
			executeUpdate(ps, 1);
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
		try (PreparedStatement ps = prepare("DELETE FROM exams.CERTRIDE_SCRIPTS WHERE (CERTNAME=?) AND (IDX=?)")) {
			ps.setString(1, id.getName());
			ps.setInt(2, id.getIndex());
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to write specific Certification pre-requsite.
	 */
	private void writePrereq(String certName, String prereqAbbr) throws SQLException {
		if (!StringUtils.isEmpty(prereqAbbr)) {
			try (PreparedStatement ps = prepareWithoutLimits("REPLACE INTO exams.CERTPREREQ (NAME, ABBR) VALUES (?, ?)")) {
				ps.setString(1, certName);
				ps.setString(2, prereqAbbr);
				executeUpdate(ps, 1);
			}
		} else {
			try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM exams.CERTPREREQ WHERE (NAME=?)")) {
				ps.setString(1, certName);
				executeUpdate(ps, 0);
			}
		}
	}
	
	/*
	 * Helper method to write security role names.
	 */
	private void writeRoles(String certName, Collection<String> roles) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.CERTROLES (CERTNAME, ROLE) VALUES (?, ?)")) {
			ps.setString(1, certName);
			for (String role : roles) {
				ps.setString(2, role);
				ps.addBatch();			
			}
		
			executeUpdate(ps, 1, roles.size());
		}
	}
	
	/*
	 * Helper method to write exam names.
	 */
	private void writeExams(String certName, Collection<String> exams) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.CERTEXAMS (CERTNAME, EXAMNAME) VALUES (?, ?)")) {
			ps.setString(1, certName);
			for (String exam : exams) {
				ps.setString(2, exam);
				ps.addBatch();
			}
		
			executeUpdate(ps, 1, exams.size());
		}
	}
	
	/*
	 * Helper method to write virtual ailrine names.
	 */
	private void writeApps(String certName, Collection<AirlineInformation> airlines) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.CERTAPPS (CERTNAME, AIRLINE) VALUES (?, ?)")) {
			ps.setString(1, certName);
			for (AirlineInformation ai : airlines) {
				ps.setString(2, ai.getCode());
				ps.addBatch();
			}

			executeUpdate(ps, 1, airlines.size());
		}
	}
	
	/*
	 * Helper method to write check ride equipment types.
	 */
	private void writeEQ(String certName, Collection<String> eqTypes) throws SQLException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO exams.CERTEQ (CERTNAME, EQTYPE) VALUES (?, ?)")) {
			ps.setString(1, certName);
			for (String eqType : eqTypes) {
				ps.setString(2, eqType);
				ps.addBatch();
			}

			executeUpdate(ps, 1, eqTypes.size());
		}
	}
}