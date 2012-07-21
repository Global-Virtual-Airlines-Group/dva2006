// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.IMAddress;

/**
 * A Data Access Object to fetch Facebook page access tokens. 
 * @author Luke
 * @version 4.2
 * @since 4.2
 */

public class GetFacebookPageToken extends DAO {
	
	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetFacebookPageToken(Connection c) {
		super(c);
	}

	/**
	 * Loads all Facebook page access tokens. 
	 * @return a List of tokens
	 * @throws DAOException if a JDBC error occurs
	 */
	public List<String> getAllTokens() throws DAOException {
		try {
			prepareStatementWithoutLimits("SELECT ADDR FROM PILOT_IMADDR WHERE (TYPE=?)");
			_ps.setString(1, IMAddress.FBPAGE.name());
			
			List<String> results = new ArrayList<String>();
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