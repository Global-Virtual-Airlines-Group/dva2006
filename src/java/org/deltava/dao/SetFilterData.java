// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

/**
 * A Data Access Object to write Content Filtering lists.
 * @author Luke
 * @version 11.0
 * @since 11.0
 */

public class SetFilterData extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetFilterData(Connection c) {
		super(c);
	}

	/**
	 * Adds a Content Filtering keyword to the database.
	 * @param kw the keyword
	 * @param isSafe TRUE if safe, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void add(String kw, boolean isSafe) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("INSERT INTO CONTENT_FILTER (KEYWORD, UPDATEDON, SAFE) VALUES (?, NOW(), ?) AS N ON DUPLICATE KEY UPDATE N.UPDATEDON=NOW()")) {
			ps.setString(1, kw);
			ps.setBoolean(2, isSafe);
			executeUpdate(ps, 1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}

	/**
	 * Deletes a Content Filtering keyword from the database.
	 * @param kw the keyword
	 * @param isSafe TRUE if safe, otherwise FALSE
	 * @throws DAOException if a JDBC error occurs
	 */
	public void delete(String kw, boolean isSafe) throws DAOException {
		try (PreparedStatement ps = prepareWithoutLimits("DELETE FROM CONTENT_FILTER WHERE (KEYWORD=?) AND (SAFE=?)")) {
			ps.setString(1, kw);
			ps.setBoolean(2, isSafe);
			executeUpdate(ps, 0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}