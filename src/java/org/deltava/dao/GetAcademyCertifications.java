// Copyright 2006, 2007, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to load Flight Academy Certifications and Check Ride scripts. 
 * @author Luke
 * @version 3.4
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
			prepareStatementWithoutLimits("SELECT * FROM exams.CERTS WHERE (NAME=?) OR (ABBR=?) LIMIT 1");
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
			prepareStatement("SELECT C.*, COUNT(CR.SEQ) FROM exams.CERTS C LEFT JOIN exams.CERTREQS CR ON "
					+ "(C.NAME=CR.CERTNAME) WHERE (C.ACTIVE=?) GROUP BY C.NAME ORDER BY C.STAGE, C.NAME");
			_ps.setBoolean(1, true);
			Collection<Certification> results = execute();
			
			// Load the exams
			for (Iterator<Certification> i = results.iterator(); i.hasNext(); ) {
				Certification c = i.next();
				loadExams(c);
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
			prepareStatement("SELECT C.*, COUNT(CR.SEQ) FROM exams.CERTS C LEFT JOIN exams.CERTREQS CR ON " 
					+ "(C.NAME=CR.CERTNAME) GROUP BY C.NAME ORDER BY C.STAGE, C.NAME");
			Collection<Certification> results = execute();
			
			// Load the exams
			for (Iterator<Certification> i = results.iterator(); i.hasNext(); ) {
				Certification c = i.next();
				loadExams(c);
			}
			
			return results;
		} catch (SQLException se) { 
			throw new DAOException(se);
		}
	}
	
	/**
	 * Loads a specific Flight Academy Check Ride script from the datbaase.
	 * @param certName the Certification name
	 * @return an AcademyRideScript bean, or null if none
	 * @throws DAOException if a JDBC error occurs
	 */
	public AcademyRideScript getScript(String certName) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT * FROM exams.CERTRIDE_SCRIPTS WHERE (CERTNAME=?)");
			_ps.setString(1, certName);
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
	
	/**
	 * Helper method to parse Check Ride script result sets.
	 */
	private List<AcademyRideScript> executeScript() throws SQLException {
		ResultSet rs = _ps.executeQuery();
		List<AcademyRideScript> results = new ArrayList<AcademyRideScript>();
		while (rs.next()) {
			AcademyRideScript sc = new AcademyRideScript(rs.getString(1));
			sc.setDescription(rs.getString(2));
			results.add(sc);
		}
		
		// Clean up
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to parse Certification result sets.
	 */
	private List<Certification> execute() throws SQLException {
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasReqCount = (rs.getMetaData().getColumnCount() > 9);
		
		// Iterate through the results
		List<Certification> results = new ArrayList<Certification>();
		while (rs.next()) {
			Certification cert = new Certification(rs.getString(1));
			cert.setCode(rs.getString(2));
			cert.setStage(rs.getInt(3));
			cert.setReqs(rs.getInt(4));
			cert.setActive(rs.getBoolean(6));
			cert.setAutoEnroll(rs.getBoolean(7));
			cert.setHasCheckRide(rs.getBoolean(8));
			cert.setDescription(rs.getString(9));
			if (cert.getReqs() == Certification.REQ_SPECIFIC)
				cert.setReqCert(rs.getString(5));
			if (hasReqCount)
				cert.setReqCount(rs.getInt(10));
			
			results.add(cert);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
	
	/**
	 * Helper method to load requirements.
	 */
	private void loadRequirements(Certification cert) throws SQLException {
		
		// Prepare the statement
		prepareStatementWithoutLimits("SELECT SEQ, EXAMNAME, REQENTRY FROM exams.CERTREQS WHERE (CERTNAME=?) ORDER BY SEQ");
		_ps.setString(1, cert.getName());
		
		// Load the result set
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			CertificationRequirement cr = new CertificationRequirement(rs.getInt(1));
			cr.setExamName(rs.getString(2));
			cr.setText(rs.getString(3));
			cert.addRequirement(cr);
		}
		
		// Clean up
		rs.close();
		_ps.close();
	}

	/**
	 * Helper method to load examinations.
	 */
	private void loadExams(Certification cert) throws SQLException {
		
		// Prepare the statement
		prepareStatementWithoutLimits("SELECT EXAMNAME FROM exams.CERTEXAMS WHERE (CERTNAME=?)");
		_ps.setString(1, cert.getName());
		
		// Load the result set
		ResultSet rs = _ps.executeQuery();
		while (rs.next())
			cert.addExamName(rs.getString(1));
		
		// Clean up
		rs.close();
		_ps.close();
	}
}