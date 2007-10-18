// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.io.File;
import java.sql.*;
import java.util.*;

import org.deltava.beans.academy.TrainingVideo;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Videos.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetAcademyVideos extends GetLibrary {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetAcademyVideos(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the contents of the Video Library.
	 * @return a List of TrainingVideo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<TrainingVideo> getVideos() throws DAOException {
		try {
			prepareStatement("SELECT V.*, 0 FROM exams.VIDEOS V ORDER BY V.NAME");
			return loadVideos();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Returns the contents of the Video Library <i>in the current database</i>.
	 * @param certName the Certification name, or null if not associated with any Academy certification
	 * @return a List of TrainingVideo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<TrainingVideo> getVideos(String certName) throws DAOException {
		try {
			prepareStatement("SELECT V.*, 0 FROM exams.VIDEOS V LEFT JOIN exams.CERTVIDEOS VC ON "
					+ "(V.FILENAME=VC.FILENAME) WHERE (VC.CERTNAME=?) GROUP BY V.NAME ORDER BY V.NAME");
			_ps.setString(1, certName);
			return loadVideos();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse result sets.
	 */
	private List<TrainingVideo> loadVideos() throws SQLException {
		
		// Execute the query
		ResultSet rs = _ps.executeQuery();
		
		// Iterate through the results
		String path = SystemData.get("path.video");
		Map<String, TrainingVideo> results = new LinkedHashMap<String, TrainingVideo>();
		while (rs.next()) {
			File f = new File(path, rs.getString(1));
			TrainingVideo entry = new TrainingVideo(f.getPath());
			entry.setName(rs.getString(2));
			entry.setCategory(rs.getString(3));
			entry.setSecurity(rs.getInt(5));
			entry.setAuthorID(rs.getInt(6));
			entry.setDescription(rs.getString(7));
			entry.setDownloadCount(rs.getInt(8));
			results.put(f.getName(), entry);
		}
			
		// Clean up
		rs.close();
		_ps.close();
		
		// Lload certification data
		prepareStatementWithoutLimits("SELECT * FROM exams.CERTVIDEOS");
		rs = _ps.executeQuery();
		while (rs.next()) {
			TrainingVideo v = results.get(rs.getString(2));
			if (v != null)
				v.addCertification(rs.getString(1));
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return new ArrayList<TrainingVideo>(results.values());
	}
	
	/**
	 * Retrieves all Flight Academy certifications associated with a particular video.
	 * @param fName the video file name
	 * @return a Collection of certification names
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getCertifications(String fName) throws DAOException {
		
		Collection<String> results = new ArrayList<String>();
		try {
			prepareStatementWithoutLimits("SELECT CERTNAME FROM exams.CERTVIDEOS WHERE (FILENAME=?)");
			_ps.setString(1, fName);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(rs.getString(1));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}