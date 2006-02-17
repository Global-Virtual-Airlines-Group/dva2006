// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
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
			setQueryMax(1);
			prepareStatement("SELECT * FROM CERTS WHERE (NAME=?)");
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
			prepareStatement("SELECT C.*, COUNT(CR.SEQ) FROM CERTS C, CERTREQS CRS WHERE (C.ACTIVE=?) "
					+ "AND (C.NAME=CR.CERTNAME) GROUP BY C.NAME ORDER BY C.STAGE, C.NAME");
			_ps.setBoolean(1, true);
			return execute();
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
			prepareStatement("SELECT C.*, COUNT(CR.SEQ) FROM CERTS C, CERTREQS CR WHERE (C.NAME=CR.CERTNAME) "
					+ "GROUP BY C.NAME ORDER BY C.STAGE, C.NAME");
			return execute();
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
		ResultSetMetaData rmd = rs.getMetaData();
		boolean hasReqCount = (rmd.getColumnCount() > 4);
		
		// Iterate through the results
		List<Certification> results = new ArrayList<Certification>();
		while (rs.next()) {
			Certification cert = new Certification(rs.getString(1));
			cert.setStage(rs.getInt(2));
			cert.setReqs(rs.getInt(3));
			cert.setActive(rs.getBoolean(4));
			if (hasReqCount)
				cert.setReqCount(rs.getInt(5));
			
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
		prepareStatementWithoutLimits("SELECT SEQ, REQENTRY FROM CERTREQS WHERE (CERTNAME=?) ORDER BY SEQ");
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
		prepareStatementWithoutLimits("SELECT EXAMNAME FROM CERTEXAMS WHERE (CERTNAME=?)");
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