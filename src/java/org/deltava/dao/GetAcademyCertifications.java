// Copyright 2006, 2007, 2010, 2011, 2014, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.academy.*;

import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Flight Academy Certifications and Check Ride scripts. 
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class GetAcademyCertifications extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAcademyCertifications(Connection c) {
		super(c);
	}

	/**
	 * Loads a Flight Academy Certification profile.
	 * @param name the Certification name
	 * @return a Certification bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public Certification get(String name) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT C.*, CPR.ABBR FROM exams.CERTS C LEFT JOIN exams.CERTPREREQ CPR ON (C.NAME=CPR.NAME) WHERE (C.NAME=?) OR (C.ABBR=?) LIMIT 1");
			_ps.setString(1, name);
			_ps.setString(2, name);
			
			// Execute the query
			List<Certification> results = execute();
			if (results.isEmpty())
				return null;
			
			// Get the first cert and load requirements/exams
			Certification cert = results.get(0);
			loadRequirements(cert);
			loadExams(cert);
			loadRoles(cert);
			loadAirlines(cert);
			loadEQ(cert);
			return cert;
		} catch (SQLException se) { 
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all active Flight Academy Certification profiles.
	 * @return a Collection of Certification beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Certification> getActive() throws DAOException {
		try {
			prepareStatement("SELECT C.*, CPR.ABBR, COUNT(CR.SEQ) FROM exams.CERTS C LEFT JOIN exams.CERTPREREQ CPR ON (C.NAME=CPR.NAME) LEFT JOIN exams.CERTREQS CR "
				+ "ON (C.NAME=CR.CERTNAME) WHERE (C.ACTIVE=?) GROUP BY C.NAME ORDER BY C.STAGE, C.NAME");
			_ps.setBoolean(1, true);
			Collection<Certification> results = execute();
			for (Certification c: results) {
				loadExams(c);
				loadRoles(c);
				loadAirlines(c);
				loadEQ(c);
			}
			
			return results;
		} catch (SQLException se) { 
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all Flight Academy Certification profiles.
	 * @return a Collection of Certification beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Certification> getAll() throws DAOException {
		try {
			prepareStatement("SELECT C.*, CPR.ABBR, COUNT(CR.SEQ) FROM exams.CERTS C LEFT JOIN exams.CERTPREREQ CPR ON (C.NAME=CPR.NAME) LEFT JOIN exams.CERTREQS CR "
				+ "ON (C.NAME=CR.CERTNAME) GROUP BY C.NAME ORDER BY C.STAGE, C.NAME");
			Collection<Certification> results = execute();
			for (Certification c : results) {
				loadExams(c);
				loadRoles(c);
				loadAirlines(c);
				loadEQ(c);
			}
			
			return results;
		} catch (SQLException se) { 
			throw new DAOException(se);
		}
	}

	/**
	 * Loads all Flight Academy Certification profiles with at least one Graduate.
	 * @param visibleOnly TRUE to only list visible Certifications, otherwise FALSE
	 * @return a Collection of Certification beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Certification> getWithGraduates(boolean visibleOnly) throws DAOException {
		try {
			prepareStatement("SELECT C.*, CPR.ABBR, COUNT(CR.SEQ) FROM exams.COURSES CS, exams.CERTS C LEFT JOIN exams.CERTPREREQ CPR ON (C.NAME=CPR.NAME) "
				+ "LEFT JOIN exams.CERTREQS CR ON (C.NAME=CR.CERTNAME) WHERE (C.NAME=CS.CERTNAME) AND (CS.STATUS=?) GROUP BY C.NAME ORDER BY C.STAGE, C.NAME;");
			_ps.setInt(1, Status.COMPLETE.ordinal());
			Collection<Certification> results = execute();
			for (Iterator<Certification> i = results.iterator(); i.hasNext(); ) {
				Certification c = i.next();
				if (visibleOnly && !c.getVisible()) {
					i.remove();
					continue;
				}
				
				loadExams(c);
				loadRoles(c);
				loadAirlines(c);
				loadEQ(c);
			}

			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads a specific Flight Academy Check Ride script from the datbaase.
	 * @param id the Check Ride ID
	 * @return an AcademyRideScript bean, or null if none
	 * @throws DAOException if a JDBC error occurs
	 */
	public AcademyRideScript getScript(AcademyRideID id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM exams.CERTRIDE_SCRIPTS WHERE (CERTNAME=?) AND (IDX=?)");
			_ps.setString(1, id.getName());
			_ps.setInt(2, id.getIndex());
			List<AcademyRideScript> results = executeScript();
			return results.isEmpty() ? null : results.get(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads all Flight Academy Check Ride scripts from the database.
	 * @return a Collection of AcademyRideScript beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<AcademyRideScript> getScripts() throws DAOException {
		try {
			prepareStatement("SELECT * FROM exams.CERTRIDE_SCRIPTS");
			return executeScript();
		} catch (SQLException se) {
			throw new DAOException(se);
		}		
	}
	
	/*
	 * Helper method to parse Check Ride script result sets.
	 */
	private List<AcademyRideScript> executeScript() throws SQLException {
		List<AcademyRideScript> results = new ArrayList<AcademyRideScript>();
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				AcademyRideScript sc = new AcademyRideScript(rs.getString(1), rs.getInt(2));
				sc.setDescription(rs.getString(3));
				results.add(sc);
			}
		}
		
		_ps.close();
		return results;
	}
	
	/*
	 * Helper method to parse Certification result sets.
	 */
	private List<Certification> execute() throws SQLException {
		List<Certification> results = new ArrayList<Certification>();
		try (ResultSet rs = _ps.executeQuery()) {
			boolean hasReqCount = (rs.getMetaData().getColumnCount() > 14);
			while (rs.next()) {
				Certification cert = new Certification(rs.getString(1));
				cert.setCode(rs.getString(2));
				cert.setStage(rs.getInt(3));
				cert.setReqs(rs.getInt(4));
				cert.setActive(rs.getBoolean(5));
				cert.setAutoEnroll(rs.getBoolean(6));
				cert.setVisible(rs.getBoolean(7));
				cert.setRideCount(rs.getInt(8));
				cert.setEquipmentProgram(rs.getString(9));
				cert.setFlightCount(rs.getInt(10));
				cert.setNetwork(OnlineNetwork.fromName(rs.getString(11)));
				cert.setNetworkRatingCode(rs.getString(12));
				cert.setDescription(rs.getString(13));
				if (cert.getReqs() == Certification.REQ_SPECIFIC)
					cert.setReqCert(rs.getString(14));
				if (hasReqCount)
					cert.setReqCount(rs.getInt(15));
			
				results.add(cert);
			}
		}
			
		_ps.close();
		return results;
	}
	
	/*
	 * Helper method to load requirements.
	 */
	private void loadRequirements(Certification cert) throws SQLException {
		prepareStatementWithoutLimits("SELECT SEQ, EXAMNAME, REQENTRY FROM exams.CERTREQS WHERE (CERTNAME=?) ORDER BY SEQ");
		_ps.setString(1, cert.getName());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				CertificationRequirement cr = new CertificationRequirement(rs.getInt(1));
				cr.setExamName(rs.getString(2));
				cr.setText(rs.getString(3));
				cert.addRequirement(cr);
			}
		}
		
		_ps.close();
	}
	
	/*
	 * Helper method to load roles.
	 */
	private void loadRoles(Certification cert) throws SQLException {
		prepareStatementWithoutLimits("SELECT ROLE FROM exams.CERTROLES WHERE (CERTNAME=?)");
		_ps.setString(1, cert.getName());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next())
				cert.addRole(rs.getString(1));
		}
		
		_ps.close();
	}

	/*
	 * Helper method to load examinations.
	 */
	private void loadExams(Certification cert) throws SQLException {
		prepareStatementWithoutLimits("SELECT EXAMNAME FROM exams.CERTEXAMS WHERE (CERTNAME=?)");
		_ps.setString(1, cert.getName());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next())
				cert.addExamName(rs.getString(1));
		}
		
		_ps.close();
	}

	/*
	 * Helper method to load virtual airlines.
	 */
	private void loadAirlines(Certification cert) throws SQLException {
		prepareStatementWithoutLimits("SELECT AIRLINE FROM exams.CERTAPPS WHERE (CERTNAME=?)");
		_ps.setString(1, cert.getName());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next())
				cert.addAirline(SystemData.getApp(rs.getString(1)));
		}
		
		_ps.close();
	}

	/*
	 * Helper method to load check ride equipment types.
	 */
	private void loadEQ(Certification cert) throws SQLException {
		prepareStatementWithoutLimits("SELECT EQTYPE FROM exams.CERTEQ WHERE (CERTNAME=?)");
		_ps.setString(1, cert.getName());
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next())
				cert.addRideEQ(rs.getString(1));
		}

		_ps.close();
	}
}