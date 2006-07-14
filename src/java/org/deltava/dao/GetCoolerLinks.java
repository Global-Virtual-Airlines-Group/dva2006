// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

/**
 * A Data Access Object to load Water Cooler image links
 * @author Luke
 * @version 1.0
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
			ResultSet rs = _ps.executeQuery();
			while (rs.next())
				results.add(new Integer(rs.getInt(1)));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Image URLs for a particular Messasge Thread.
	 * @param id the Message Thread database ID
	 * @return a Collection of URLs
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<String> getURLs(int id) throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT URL FROM common.COOLER_IMGURLS WHERE (ID=?)");
			_ps.setInt(1, id);
			
			// Execute the query
			Collection<String> results = new LinkedHashSet<String>();
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