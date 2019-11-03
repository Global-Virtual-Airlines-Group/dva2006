// Copyright 2013, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao;

import java.sql.*;
import java.util.Collection;

import org.deltava.beans.system.*;

/**
 * A Data Access Object to write IP netblock data.
 * @author Luke
 * @version 8.7
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
	
	/**
	 * Writes an IP location entry to the database.
	 * @param loc an IPLocation bean
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(IPLocation loc) throws DAOException {
		try {
			prepareStatementWithoutLimits("INSERT INTO geoip.LOCATIONS (ID, COUNTRY, REGION, REGION_NAME, CITY) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY "
				+ "UPDATE COUNTRY=VALUES(COUNTRY), REGION=VALUES(REGION), REGION_NAME=VALUES(REGION_NAME), CITY=VALUES(CITY)");
			_ps.setInt(1, loc.getID());
			_ps.setString(2, loc.getCountry().getCode());
			_ps.setString(3, loc.getRegionCode());
			_ps.setString(4, loc.getRegion());
			_ps.setString(5, loc.getCityName());
			executeUpdate(1);
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
	
	/**
	 * Writes an IP network block to the database.
	 * @param blocks a Collection of IPBlocks
	 * @throws DAOException if a JDBC error occurs
	 */
	public void write(Collection<IPBlock> blocks) throws DAOException {
		try {
			prepareStatementWithoutLimits("REPLACE INTO geoip.BLOCKS (ID, BLOCK_START, BLOCK_END, BITS, LOCATION_ID, LAT, LNG, RADIUS) VALUES (?, INET6_ATON(?), INET6_ATON(?), ?, ?, ?, ?, ?)");
			for (IPBlock ib : blocks) {
				_ps.setInt(1, ib.getID());
				_ps.setString(2, ib.getAddress());
				_ps.setString(3, ib.getLastAddress());
				_ps.setInt(4, ib.getBits());
				_ps.setInt(5, Integer.parseInt(ib.getCity()));
				_ps.setDouble(6, ib.getLatitude());
				_ps.setDouble(7, ib.getLongitude());
				_ps.setInt(8, ib.getRadius());
				_ps.addBatch();
			}
			
			executeBatchUpdate(1, blocks.size());
		} catch (SQLException se) {
			throw new DAOException(se);
		}
	}
}