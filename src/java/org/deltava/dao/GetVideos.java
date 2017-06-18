// Copyright 2006, 2007, 2008, 2011, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.io.File;
import java.sql.*;
import java.util.*;

import org.deltava.beans.fleet.Security;
import org.deltava.beans.fleet.Video;
import org.deltava.beans.academy.TrainingVideo;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to load Videos.
 * @author Luke
 * @version 7.5
 * @since 1.0
 */

public class GetVideos extends GetLibrary {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetVideos(Connection c) {
		super(c);
	}
	
	/**
	 * Returns the contents of the Video Library.
	 * @return a List of TrainingVideo beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Video> getVideos() throws DAOException {
		try {
			prepareStatement("SELECT V.*, GROUP_CONCAT(VC.CERT) FROM exams.VIDEOS V LEFT JOIN exams.CERTVIDEOS VC ON (V.FILENAME=VC.FILENAME) GROUP BY V.NAME");
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
	public Collection<Video> getVideos(String certName) throws DAOException {
		try {
			prepareStatement("SELECT V.*, GROUP_CONCAT(VC.CERT) FROM exams.VIDEOS V LEFT JOIN exams.CERTVIDEOS VC ON (V.FILENAME=VC.FILENAME) WHERE (VC.CERT=?) GROUP BY V.NAME");
			_ps.setString(1, certName);
			return loadVideos();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse result sets.
	 */
	private List<Video> loadVideos() throws SQLException {
		List<Video> results = new ArrayList<Video>();
		File p = new File(SystemData.get("path.video"));
		try (ResultSet rs = _ps.executeQuery()) {
			while (rs.next()) {
				Video entry = null;
				File f = new File(p, rs.getString(1)); 
				Collection<String> certs = StringUtils.split(rs.getString(8), ",");
				if (!CollectionUtils.isEmpty(certs)) {
					TrainingVideo tv = new TrainingVideo(f);
					tv.setCertifications(certs);
					entry = tv;
				} else
					entry = new Video(f);
			
				entry.setName(rs.getString(2));
				entry.setCategory(rs.getString(3));
				entry.setSecurity(Security.values()[rs.getInt(5)]);
				entry.setAuthorID(rs.getInt(6));
				entry.setDescription(rs.getString(7));
				results.add(entry);
			}
		}
			
		_ps.close();
		return results;
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
			prepareStatementWithoutLimits("SELECT CERT FROM exams.CERTVIDEOS WHERE (FILENAME=?)");
			_ps.setString(1, fName);
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));	
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}