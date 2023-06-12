// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

/**
 * A Data Access Object to populate Content Filtering lists.
 * @author Luke
 * @version 11.0
 * @since 11.0
 */

public class GetFilterData extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFilterData(Connection c) {
		super(c);
	}

	/**
	 * Returns all keywords to trigger on.
	 * @param isSafe TRUE for safe words, otherwise FALSE
	 * @return a Collection of keywords
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<String> getKeywords(boolean isSafe) throws DAOException {
		try (PreparedStatement ps = prepare("SELECT KEYWORD FROM CONTENT_FILTER WHERE (SAFE=?)")) {
			ps.setBoolean(1, isSafe);
			List<String> results = new ArrayList<String>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					results.add(rs.getString(1));
			}
			
			return results;
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}