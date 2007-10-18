// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.*;

/**
 * A Data Access Object to load Flight Academy Certifications. 
 * @author Luke
 * @version 1.0
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
			prepareStatementWithoutLimits("SELECT * FROM exams.CERTS WHERE (NAME=?) LIMIT 1");
			_ps.setString(1, name);
			
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
	 * Helper method to parse result sets.
	 */
	private List<Certification> execute() throws SQLException {
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		boolean hasReqCount = (rs.getMetaData().getColumnCount() > 6);
		
		// Iterate through the results
		List<Certification> results = new ArrayList<Certification>();
		while (rs.next()) {
			Certification cert = new Certification(rs.getString(1));
			cert.setCode(rs.getString(2));
			cert.setStage(rs.getInt(3));
			cert.setReqs(rs.getInt(4));
			cert.setActive(rs.getBoolean(5));
			cert.setAutoEnroll(rs.getBoolean(6));
			if (hasReqCount)
				cert.setReqCount(rs.getInt(7));
			
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
		prepareStatementWithoutLimits("SELECT SEQ, REQENTRY FROM exams.CERTREQS WHERE (CERTNAME=?) ORDER BY SEQ");
		_ps.setString(1, cert.getName());
		
		// Load the result set
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			CertificationRequirement cr = new CertificationRequirement(rs.getInt(1));
			cr.setText(rs.getString(2));
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