// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.HelpEntry;

/**
 * A Data Access Object to load Help entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GetHelp extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC Connection to use
	 */
	public GetHelp(Connection c) {
		super(c);
	}

	/**
	 * Returns a particular Online Help Entry.
	 * @param id the entry title
	 * @return a HelpEntry bean, or null if not found
	 * @throws DAOException if a JDBC error occurs
	 */
	public HelpEntry get(String id) throws DAOException {
		try {
			setQueryMax(1);
			prepareStatement("SELECT * FROM HELP WHERE (ID=?)");
			_ps.setString(1, id);
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				_ps.close();
				return null;
			}
				
			// Create the bean
			HelpEntry entry = new HelpEntry(rs.getString(1), rs.getString(3));
			entry.setSubject(rs.getString(2));
			
			// Clean up and return
			rs.close();
			_ps.close();
			return entry;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Returns all Online Help Entries.
	 * @return a Collection of HelpEntry beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<HelpEntry> getAll() throws DAOException {
		try {
			prepareStatement("SELECT * FROM HELP ORDER BY ID");
			
			// Execute the query
			ResultSet rs = _ps.executeQuery();

			// Iterate through the results
			Collection<HelpEntry> results = new ArrayList<HelpEntry>();
			while (rs.next()) {
				HelpEntry entry = new HelpEntry(rs.getString(1), rs.getString(3));
				entry.setSubject(rs.getString(2));
				results.add(entry);
			}
			
			// Clean up and return
			rs.close();
			_ps.close();
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}