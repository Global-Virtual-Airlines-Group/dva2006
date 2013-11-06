// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;

import org.deltava.beans.system.*;

/**
 * A Data Access Object to write IP netblock data.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public class SetIPLocation extends DAO {

	/**
	 * Initializes the Data Access Object.
	 * @param c the JDBC connection to use
	 */
	public SetIPLocation(Connection c) {
		super(c);
	}

/*
	public void write(IP4Block ib) throws DAOException {
		try {
			
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
*/	
	public void write(IP6Block ib) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO geoip.BLOCKS6 (ID, BLOCK_START, BLOCK_END, BITS, COUNTRY, LAT, LNG) "
				+ "VALUES (?, INET6_ATON(?), INET6_ATON(?), ?, ?, ?, ?)");
			_ps.setInt(1, ib.getID());
			_ps.setString(2, ib.getAddress());
			_ps.setString(3, ib.getLastAddress());
			_ps.setInt(4, ib.getBits());
			_ps.setString(5, ib.getCountry().getCode());
			_ps.setDouble(6, ib.getLatitude());
			_ps.setDouble(7, ib.getLongitude());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Purges an IP block table.
	 * @param addrType
	 * @throws DAOException if a JDBC error occurs
	 */
	public void purge(IPAddress addrType) throws DAOException {
		String tableName = (addrType == IPAddress.IPV4) ? "BLOCKS" : "BLOCKS6";
		try {
			prepareStatementWithoutLimits("DELETE FROM geoip." + tableName);
			executeUpdate(0);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}