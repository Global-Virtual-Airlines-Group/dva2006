// Copyright 2007, 2009, 2011, 2013, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.*;

/**
 * A Data Access Object to load Login IP address data.
 * @author Luke
 * @version 9.0
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
		try (PreparedStatement ps = prepare("SELECT ID, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST, LOGINS FROM SYS_LOGINS WHERE (ID=?)")) {
			ps.setInt(1, id);
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT ID, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST, LOGINS FROM SYS_LOGINS WHERE (REMOTE_HOST LIKE ?) OR (INET6_NTOA(REMOTE_ADDR) LIKE ?)")) {
			ps.setString(1, host);
			ps.setString(2, host);
			return execute(ps);
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
		try (PreparedStatement ps = prepare("SELECT ID, INET6_NTOA(REMOTE_ADDR), REMOTE_HOST, LOGINS FROM SYS_LOGINS WHERE (REMOTE_ADDR >= INET6_ATON(?)) AND (REMOTE_ADDR <= INET6_ATON(?))")) {
			ps.setString(1, addrBlock.getAddress());
			ps.setString(2, addrBlock.getLastAddress());
			return execute(ps);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/*
	 * Helper method to parse result sets.
	 */
	private static List<LoginAddress> execute(PreparedStatement ps) throws SQLException {
		List<LoginAddress> results = new ArrayList<LoginAddress>();
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				LoginAddress addr = new LoginAddress(rs.getInt(1));
				addr.setRemoteAddr(rs.getString(2));
				addr.setRemoteHost(rs.getString(3));
				addr.setLoginCount(rs.getInt(4));
				results.add(addr);
			}
		}
		
		return results;
	}
}