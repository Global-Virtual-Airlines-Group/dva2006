// Copyright 2006, 2008, 2011, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.cooler.LinkedImage;

/**
 * A Data Access Object to load Water Cooler image links.
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public class GetCoolerLinks extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetCoolerLinks(Connection c) {
		super(c);
	}

	/**
	 * Returns the database IDs of all Message Threads with Linked Images.
	 * @return a Collection of database IDs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<Integer> getThreads() throws DAOException {
		try {
			prepareStatement("SELECT DISTINCT ID FROM common.COOLER_IMGURLS ORDER BY ID");
			
			// Execute the query
			Collection<Integer> results = new LinkedHashSet<Integer>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next())
					results.add(Integer.valueOf(rs.getInt(1)));
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Image URLs for a particular Messasge Thread.
	 * @param id the Message Thread database ID
	 * @param includeDisabled TRUE if disabled links should be included, otherwise FALSE
	 * @return a Collection of URLs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LinkedImage> getURLs(int id, boolean includeDisabled) throws DAOException {
		
		// Build the SQL statement
		StringBuilder buf = new StringBuilder("SELECT SEQ, URL, COMMENTS, DISABLED FROM common.COOLER_IMGURLS WHERE (ID=?)");
		if (!includeDisabled)
			buf.append(" AND (DISABLED=?)");
		
		try {
			prepareStatementWithoutLimits(buf.toString());
			_ps.setInt(1, id);
			if (!includeDisabled)
				_ps.setBoolean(2, false);
			
			// Execute the query
			Collection<LinkedImage> results = new TreeSet<LinkedImage>();
			try (ResultSet rs = _ps.executeQuery()) {
				while (rs.next()) {
					LinkedImage img = new LinkedImage(rs.getInt(1), rs.getString(2));
					img.setDescription(rs.getString(3));
					img.setDisabled(rs.getBoolean(4));
					img.setThreadID(id);
					results.add(img);
				}
			}
			
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}