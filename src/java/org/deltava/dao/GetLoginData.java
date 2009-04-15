// Copyright 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.*;

/**
 * A Data Access Object to load Login IP address data.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class GetLoginData extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public GetLoginData(Connection c) {
		super(c);
	}
	
	/**
	 * Returns all login addresses for a particular User.
	 * @param id the user's database ID
	 * @return a Collection of LoginAddress beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LoginAddress> getAddresses(int id) throws DAOException {
		try {
			prepareStatement("SELECT ID, INET_NTOA(REMOTE_ADDR), REMOTE_HOST, LOGINS FROM SYS_LOGINS WHERE (ID=?)");
			_ps.setInt(1, id);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Searches for all user logins by a particular host name or portion thereof. 
	 * @param host the host name
	 * @return a Collection of LoginAddress beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LoginAddress> getLoginUsers(String host) throws DAOException {
		try {
			prepareStatement("SELECT ID, INET_NTOA(REMOTE_ADDR), REMOTE_HOST, LOGINS FROM SYS_LOGINS "
					+ "WHERE (REMOTE_HOST LIKE ?) OR (INET_NTOA(REMOTE_ADDR) LIKE ?)");
			_ps.setString(1, host);
			_ps.setString(2, host);
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Searches for all user logins within a particular IP network.
	 * @param address the IP address
	 * @param addrBlock the address network block
	 * @return a Collection of LoginAddress beans
	 * @throws DAOException if a JDBC error occurs
	 */
	public Collection<LoginAddress> getLoginUsers(String address, IPBlock addrBlock) throws DAOException {
		try {
			prepareStatement("SELECT ID, INET_NTOA(REMOTE_ADDR), REMOTE_HOST, LOGINS FROM SYS_LOGINS "
					+ "WHERE (REMOTE_ADDR >= INET_ATON(?)) AND (REMOTE_ADDR <= (INET_ATON(?) + ?))");
			_ps.setString(1, addrBlock.getAddress());
			_ps.setString(2, addrBlock.getAddress());
			_ps.setInt(3, addrBlock.getSize());
			return execute();
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Helper method to parse result sets.
	 */
	private Collection<LoginAddress> execute() throws SQLException {
		
		// Execute the query
		Collection<LoginAddress> results = new ArrayList<LoginAddress>();
		ResultSet rs = _ps.executeQuery();
		while (rs.next()) {
			LoginAddress addr = new LoginAddress(rs.getInt(1));
			addr.setRemoteAddr(rs.getString(2));
			addr.setRemoteHost(rs.getString(3));
			addr.setLoginCount(rs.getInt(4));
			results.add(addr);
		}
		
		// Clean up and return
		rs.close();
		_ps.close();
		return results;
	}
}